package br.com.adonio.promise

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch

@RunWith(JUnit4::class)
class PromiseTest {

    @Test
    fun testBase() {
        val semaphore = CountDownLatch(1)
        var outsider = 44
        var doner = 44

        Promise<Boolean>(200) { resolve, _ ->
                resolve(false)
            }
            .then {
                outsider = 221
                assertFalse(it)
            }
            .always {
                doner = 255
                semaphore.countDown()
            }

        semaphore.await()

        assertEquals(221, outsider)
        assertEquals(255, doner)
    }

    @Test
    fun testAlways() {
        val semaphore = CountDownLatch(1)

        Promise<Boolean>(200) { resolve, reject ->
            resolve(true)
        }.always {
            assertTrue(true)
            semaphore.countDown()
        }

        semaphore.await()
    }

    @Test
    fun testHaltResolve() {
        val semaphore = CountDownLatch(1)

        Promise<Boolean>(200) { resolve, reject ->
                resolve(true)

                fail("não pode ser executada essa linha")

                reject("tro lo lo")
            }
            .then {
                assertTrue(it)
            }
            .catch {
                fail("não pode ser executada catch")
            }
            .always {
                semaphore.countDown()
            }

        semaphore.await()
    }

    @Test
    fun testHaltReject() {
        val semaphore = CountDownLatch(1)

        var cc = 0
        Promise<Boolean>(200) { _, reject ->
                cc = 1
                reject("rapaz")

                fail("não pode ser executada essa linha")

                reject("tro lo lo")
            }
            .then {
                cc = 3
                fail("esta linha não pode ser executada")
            }
            .catch {
                assertEquals(1, cc)
                cc = 2
                assertEquals("rapaz", it)
            }
            .always {
                cc = 4
                semaphore.countDown()
            }

        semaphore.await()
        assertEquals(4, cc)
    }

}