@file:Suppress("unused")

package br.com.adonio.promise

import br.com.adonio.task.Task

class Promise<T>(delay: Long=0, cb: Dsl<T>.()->Unit) {

    private var resolver: ((T)->Unit)?= null
    private var rejector: ((String)->Unit)?=null
    private var doner: (()->Unit)?=null

    private var wasResolved = false
    private var wasRejected = false
    private var wasDone = false

    private var resolverAssigned = false
    private var rejectorAssigned = false

    init {
        Task.main(delay) {

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