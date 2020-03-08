package br.com.adonio.promise

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch

@RunWith(JUnit4::class)
class PromiseTest {

    @Test
    fun testResolver() {
        val semaphore = CountDownLatch(1)
        var outsider = 44
        var doner = 44

        Promise<Boolean>(250) {
            outsider = 25
            resolve(false)
            resolve(true)
        }.then {
            outsider++
            assertFalse(it)
        }.catch {
            fail("Not here")
        }.always {
            doner = 55
            semaphore.countDown()
        }

        semaphore.await()

        assertEquals(26, outsider)
        assertEquals(55, doner)
    }

    @Test
    fun testRejector() {
        val semaphore = CountDownLatch(1)
        var outsider = 44
        var doner = 44

        Promise<Boolean>(150) {
            outsider = 25
            reject("adon")
            reject("simple")
            outsider++
        }.then {
            fail("not here")
        }.catch {
            outsider++
            assertEquals("adon", it)
        }.always {
            doner = 55
            semaphore.countDown()
        }

        semaphore.await()

        assertEquals(27, outsider)
        assertEquals(55, doner)
    }

    @Test
    fun testAlways() {
        val semaphore = CountDownLatch(1)
        var doner = 44

        Promise<Boolean>(250) {
            reject("adon")
        }.always {
            doner++
            semaphore.countDown()
        }

        semaphore.await()

        assertEquals(45, doner)
    }

    @Test
    fun testFun() {
        val semaphore = CountDownLatch(1)
        var jj = 1

        promise<String>(250) {
            jj++
            resolve("2")
            resolve("5")
        }.then {
            assertEquals("2", it)
            jj++
        }.always {
            semaphore.countDown()
        }

        semaphore.await()

        assertEquals(3, jj)
    }

    @Test
    fun testResolveAndHalt() {
        val sem = CountDownLatch(1)
        var jar = 452

        promise<Boolean>(250) {
            jar++
            resolveAndHalt(true)
            fail("not here")
            jar++
        }.then {
            assertEquals(453, jar)
            assertTrue(it)
        }.catch {
            fail("not here")
        }.always {
            jar++
            sem.countDown()
        }

        sem.await()
        assertEquals(454, jar)
    }

    @Test
    fun testRejectAndHalt() {
        val sem = CountDownLatch(1)
        var jar = 452

        promise<Boolean>(250) {
            jar++
            rejectAndHalt("true")
            fail("not here")
            jar++
        }.then {
            fail("not here")
        }.catch {
            assertEquals(453, jar)
            assertEquals("true", it)
        }.always {
            jar++
            sem.countDown()
        }

        sem.await()
        assertEquals(454, jar)
    }

    @Test
    fun testException() {
        val count = CountDownLatch(1)

        promise<Int>(250) {
            @Suppress("DIVISION_BY_ZERO")
            val d = 4 / 0

            fail("not here")
        }.catch {
            assertEquals("divide by zero", it)
        }.always {
            count.countDown()
        }

        count.await()
    }

    @Test
    fun testFunVariable() {
        val count = CountDownLatch(2)
        var r: String? = null

        promiseIt(r, 199)
            .then { fail("not here") }
            .catch { assertEquals("null", it) }
            .always { count.countDown() }

        r = "sourcing"
        promiseIt(r, 157)
            .then { assertEquals("sourcing", it) }
            .catch { fail("not here") }
            .always { count.countDown() }

        count.await()
    }

}