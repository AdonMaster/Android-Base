@file:Suppress("unused")

package br.com.adonio.promise

import br.com.adonio.task.Task

class Promise<V>(private val delay: Long=0, private val cb: ((V) -> Unit, (String) -> Unit) -> Unit) {

    private var resolver: (V)->Unit = {}
    private var rejector: (String)->Unit = {}
    private var doner: (()->Unit)? = null

    private var resolverDefined = false
    private var rejectorDefined = false

    init {
        start()
    }

    private fun start() {
        Task.main(delay) {
            try {

                cb(resolver, rejector)

                if (!resolverDefined && !rejectorDefined) {
                    doner?.invoke()
                }

            } catch (e: PromiseHaltException) {
                doner?.invoke()
            } catch (e: Exception) {
                rejector(e.localizedMessage)
            }
        }
    }

    fun then(cb: (V)->Unit): Promise<V> {
        resolverDefined = true
        resolver = {
            cb(it)
            throw PromiseHaltException()
        }
        return this
    }

    fun catch(cb: (String)->Unit): Promise<V> {
        rejectorDefined = true
        rejector = {
            cb(it)
            throw PromiseHaltException()
        }
        return this
    }

    fun always(cb: ()->Unit) {
        doner = cb
    }

}

class PromiseHaltException: Exception("PromiseHaltException")

fun <T> promiseIt(obj: T?) = Promise<T> { resolve, reject ->
    try {
        if (obj!=null) {
            resolve(obj)
        } else {
            reject("objeto vazio")
        }
    } catch(e: Exception) {
        reject(e.localizedMessage)
    }
}