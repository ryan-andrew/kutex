@file:Suppress("DEPRECATION")

package dev.ryanandrew.kutex.mutable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MutableTryWithLockTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private val kutex = mutableKutexOf("test")

    @Test
    fun can_return_from_within_tryWithLock_scope() = scope.runTest {
        kutex.tryWithLock {
            return@runTest
        }
    }

    @Test
    fun tryWithLock_properly_locks_and_unlocks_kutex() = scope.runTest {
        val lock = Mutex(true)
        assertFalse(kutex.isLocked)
        scope.launch {
            kutex.tryWithLock {
                lock.lock()
            }
        }
        advanceUntilIdle()
        assertTrue(kutex.isLocked)
        lock.unlock()
        advanceUntilIdle()
        assertFalse(kutex.isLocked)
    }

    @Test
    fun successful_tryWithLock_suspends_until_kutex_is_unlocked() = scope.runTest {
        val lockTime = 10000L
        scope.launch {
            assertNotNull(
                kutex.tryWithLock {
                    delay(lockTime)
                }
            )
        }
        advanceTimeBy(lockTime / 2)
        kutex.withLock {
            assertEquals(lockTime, currentTime)
        }
    }

    @Test
    fun tryWithLock_returns_null_if_kutex_is_locked() = scope.runTest {
        val lockTime = 10000L
        scope.launch {
            kutex.withLock {
                delay(lockTime)
            }
        }
        advanceTimeBy(lockTime / 2)
        val actualValue = kutex.tryWithLock { "non-null String" }
        assertNull(actualValue.getOrNull())
    }

    @Test
    fun tryWithLock_does_not_execute_action_if_kutex_is_locked() = scope.runTest {
        val lockTime = 10000L
        scope.launch {
            kutex.withLock {
                delay(lockTime)
            }
        }
        advanceTimeBy(lockTime / 2)
        val expected = "expected"
        var actual = expected
        kutex.tryWithLock {
            actual = "action has been run"
        }
        assertEquals(expected, actual)
    }

    @Test
    fun tryWithLock_can_mutate_kutex_value() = scope.runTest {
        val newValue = "new value"
        kutex.tryWithLock { value = newValue }
        assertEquals(newValue, kutex.withLock { value })
    }

    @Test
    fun cannot_unlock_during_tryWithLock() {
        assertFailsWith<IllegalStateException> {
            kutex.tryWithLock {
                kutex.unlock()
            }
        }
    }
}
