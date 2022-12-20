@file:Suppress("DEPRECATION")

package dev.ryanandrew.kutex.mutable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MutableWithLockTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private val kutex = mutableKutexOf("test")

    @Test
    fun final_variable_can_be_assigned_within_withLock_scope() = scope.runTest {
        val actual: String
        val expected = kutex.withLock {
            actual = value
            value
        }
        assertEquals(expected, actual)
    }

    @Test
    fun can_return_from_within_withLock_scope() = scope.runTest {
        kutex.withLock {
            return@runTest
        }
    }

    @Test
    fun withLock_properly_locks_and_unlocks_kutex() = scope.runTest {
        val lock = Mutex(true)
        assertFalse(kutex.isLocked)
        scope.launch {
            kutex.withLock {
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
    fun withLock_suspends_until_kutex_is_unlocked() = scope.runTest {
        val lockTime = 10000L
        scope.launch {
            kutex.withLock {
                delay(lockTime)
            }
        }
        advanceTimeBy(lockTime / 2)
        kutex.withLock {
            assertEquals(lockTime, currentTime)
        }
    }

    @Test
    fun withLock_can_mutate_kutex_value() = scope.runTest {
        val newValue = "new value"
        kutex.withLock { value = newValue }
        assertEquals(newValue, kutex.withLock { value })
    }

    @Test
    fun cannot_unlock_during_withLock() = scope.runTest {
        val failed = try {
            kutex.withLock { kutex.unlock() }
            false
        } catch (e: IllegalStateException) {
            true
        }
        assertTrue(failed)
    }

    @Test
    fun unlocks_even_when_exception_is_thrown() = scope.runTest {
        try {
            kutex.withLock {
                throw IllegalStateException("some exception was thrown!")
            }
        } catch (ignored: IllegalStateException) { }
        assertFalse(kutex.isLocked)
    }
}
