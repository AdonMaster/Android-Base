package br.com.adonio.promise

import br.com.adonio.task.Task
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
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

        Promise<Boolean>(157) {
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

    @Test
    fun testSuspended() {
        val cc = CountDownLatch(2)
        var re = 0
        val p = promise<Int>(195) {
                re++
                resolve(552)
            }
            .suspended()
            .then {
                assertEquals(552, it)
            }
            .always {
                cc.countDown()
            }

        //
        Task.main(411) {
            assertEquals(0, re)

            re++
            cc.countDown()

            p.start()
        }

        cc.await()
        assertEquals(2, re)
    }

    @Test
    fun testChain() {
        val cc = CountDownLatch(1)
        var ff = 0

        chain(
            promiseIt(2, 202),
            promiseIt("sacana", 158),
            promise(200) {
                assertEquals(0, ff)
                ff++
                resolve(1)
            },
            promise(358) {
                assertEquals(1, ff)
                resolve(1)
            }
        )
            .then {
                assertEquals(2, it[0])
                assertEquals("sacana", it[1])
                assertEquals(1, it[2])
                assertEquals(1, it[3])
            }
            .catch {
                fail("not here")
            }
            .always {
                cc.countDown()
            }

        cc.await()
    }

    @Test
    fun testChainReject() {
        val cc = CountDownLatch(1)
        var rejected = ""

        chain(
            promiseIt(66, 300),
            promise(400) {
                Task.main(500) {
                    reject("kpeta")
                }
            },
            promise(600) {
                fail("not here")
            }
        )
            .then {
                fail("not here")
            }
            .catch {
                rejected = it
            }
            .always {
                cc.countDown()
            }

        cc.await()

        assertEquals("kpeta", rejected)
    }

}