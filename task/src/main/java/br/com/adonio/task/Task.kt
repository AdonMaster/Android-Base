@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package br.com.adonio.task

import android.os.Handler
import android.os.Looper

object Task {

    private val debounces by lazy<MutableList<String>> { mutableListOf() }

    fun main(delay: Long = 0, closure: ()->Unit): Boolean {
        return Handler(Looper.getMainLooper()).postDelayed(closure, delay)
    }

    fun debounce(key: String, interval: Long, cb: ()->Unit) {
        debounces.add(key)
        main(interval) {
            debounces.remove(key)
            if (debounces.indexOf(key) == -1) cb()
        }
    }

}