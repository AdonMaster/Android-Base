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
    open class Dsl<T>(private val promise: Promise<T>) {

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


class PromiseChain(private vararg val list: Dsl.()->Unit) {
    private val r = arrayListOf<Any>()

    fun build() = Promise<List<Any>> {
        callNext(this, 0)
    }

    private fun callNext(dsl: Promise.Dsl<List<Any>>, index: Int) {
        val p = list.getOrNull(index)
        if (p == null) {

            dsl.resolve(r)

        } else {
            val priorValue = r.getOrNull(index - 1)

            val innerDsl = Dsl(
                priorValue,
                promise<Any> {}
                    .then {

                        r.add(it)
                        callNext(dsl, index + 1)

                    }.catch {
                        dsl.reject(it)
                    }
            )

            // call
            try {
                p(innerDsl)
            } catch (e: Promise.HaltException) {
                //
            } catch (e: Exception) {
                dsl.reject(e.localizedMessage)
            }
        }
    }

    //

    class Dsl(private var prior: Any?, promise: Promise<Any>): Promise.Dsl<Any>(promise) {
        fun <T> getPriorValue(): T {
            @Suppress("UNCHECKED_CAST")
            return prior as T
        }
    }
}

fun chain(vararg dsls: PromiseChain.Dsl.()->Unit): Promise<List<Any>> {
    return PromiseChain(*dsls).build()
}