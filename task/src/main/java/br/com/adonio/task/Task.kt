@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package br.com.adonio.task

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Task {

    private val debounces by lazy<MutableList<String>> { mutableListOf() }

    fun main(delayInMilliSeconds: Long = 0, closure: ()->Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(delayInMilliSeconds)
            closure()
        }
    }

    fun debounce(key: String, interval: Long, cb: ()->Unit) {
        debounces.add(key)
        main(interval) {
            debounces.remove(key)
            if (debounces.indexOf(key) == -1) cb()
        }
    }

}