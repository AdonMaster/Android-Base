@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package br.com.adonio.promise

import br.com.adonio.task.Task

class Promise<T>(delay: Long=0, private val cb: Dsl<T>.()->Unit) {

    private var resolver: ((T)->Unit)?= null
    private var rejector: ((String)->Unit)?=null
    private var doner: (()->Unit)?=null

    private var wasStarted = false
    private var wasResolved = false
    private var wasRejected = false
    private var wasDone = false

    private var resolverAssigned = false
    private var rejectorAssigned = false

    private var isSuspended = false

    init {
        Task.main(delay) {

            if (!isSuspended) {
                start()
            }
        }
    }

    fun start() {
        if (wasStarted) { return }
        wasStarted = true

        try {
            cb(Dsl(this))
        } catch (e: HaltException) {
            doner?.invoke()
        } catch (e: Exception) {
            rejector?.invoke(e.localizedMessage)
        }

        if (!resolverAssigned && !rejectorAssigned) {
            doner?.invoke()
        }
    }

    fun suspended(): Promise<T> {
        this.isSuspended = true
        return this
    }

    fun then(cb: (T)->Unit): Promise<T> {
        resolverAssigned = true
        resolver = {
            if (!wasResolved) {
                wasResolved = true
                cb(it)

                doner?.invoke()
            }
        }
        return this
    }

    fun catch(cb: (String)->Unit): Promise<T> {
        rejectorAssigned = true
        rejector = {
            if (!wasRejected) {
                wasRejected = true
                cb(it)

                doner?.invoke()
            }
        }
        return this
    }

    fun always(cb: ()->Unit): Promise<T> {
        doner = {
            if (!wasDone) {
                wasDone = true
                cb()
            }
        }
        return this
    }

    //
    class Dsl<T>(private val promise: Promise<T>) {

        fun resolve(value: T) {
            promise.resolver?.invoke(value)
        }

        fun resolveAndHalt(value: T) {
            resolve(value)
            throw HaltException()
        }

        fun reject(reason: String) {
            promise.rejector?.invoke(reason)
        }

        fun rejectAndHalt(reason: String) {
            reject(reason)
            throw HaltException()
        }

    }

    //
    class HaltException: Exception("halt")

}

fun <T> promise(delay: Long = 0, cb: Promise.Dsl<T>.()->Unit): Promise<T> {
    return Promise(delay, cb)
}

fun <T> promiseIt(value: T?, delay: Long=0): Promise<T> {
    return promise(delay) {
        value?.let { resolveAndHalt(it) }
        reject("null")
    }
}

class PromiseChain(vararg promises: Promise<Any>) {
    private val r = arrayListOf<Any>()
    private val list: List<Promise<Any>>

    init {
        for (p in promises) {
            p.suspended()
        }
        list = promises.toList()
    }

    fun build() = Promise<List<Any>> {
        callNext(0, this)
    }

    private fun callNext(index: Int, dsl: Promise.Dsl<List<Any>>) {
        val p = list.getOrNull(index)
        if (p == null) {

            dsl.resolve(r)

        } else {

            p.then {

                r.add(it)
                callNext(index + 1, dsl)

            }.catch {
                dsl.reject(it)
            }.start()

        }
    }
}

fun chain(vararg promises: Promise<Any>): Promise<List<Any>> {
    return PromiseChain(*promises).build()
}