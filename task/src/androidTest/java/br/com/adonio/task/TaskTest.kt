package br.com.adonio.task

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch

@RunWith(JUnit4::class)
class TaskTest {

    @Test
    fun debounce() {
        val semaphore = CountDownLatch(1)
        var cabloco = 2

        Task.debounce("dinossauro", 2000) {
            cabloco = 252
            semaphore.countDown()
        }
        Task.debounce("dinossauro", 2000) {
            cabloco = 661
            semaphore.countDown()
        }

        semaphore.await()

        assertEquals(661, cabloco)
    }
}