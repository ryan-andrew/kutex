@file:Suppress("DEPRECATION")

package dev.ryanandrew.kutex.immutable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class KutexTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private val value = "test"
    private val kutex = kutexOf(value)

    @Test
    fun isLocked_is_initially_false() = scope.runTest {
        assertFalse(kutex.isLocked)
    }

    @Test
    fun lock_sets_isLocked_to_true() = scope.runTest {
        assertFalse(kutex.isLocked)
        kutex.lock()
        assertTrue(kutex.isLocked)
    }

    @Test
    fun unlock_sets_isLocked_to_false() = scope.runTest {
        assertFalse(kutex.isLocked)
        kutex.lock()
        assertTrue(kutex.isLocked)
        kutex.unlock()
        assertFalse(kutex.isLocked)
    }

    @Test
    fun unlock_on_unlocked_kutex_throws() = scope.runTest {
        assertFailsWith<IllegalStateException> { kutex.unlock() }
    }

    @Test
    fun lock_suspends_until_unlocked() = scope.runTest {
        val lockTime = 10000L
        scope.launch {
            kutex.lock()
            delay(lockTime)
            kutex.unlock()
        }
        advanceTimeBy(lockTime / 2)
        kutex.lock()
        assertEquals(currentTime, lockTime)
    }

    @Test
    fun tryLock_on_unlocked_kutex_acquires_lock() = scope.runTest {
        assertTrue(kutex.tryLock())
        assertTrue(kutex.isLocked)
    }

    @Test
    fun tryLock_on_locked_kutex_does_not_acquire_lock() = scope.runTest {
        kutex.lock()
        assertFalse(kutex.tryLock())
    }

    @Test
    fun accessing_value_while_not_locked_throws() = scope.runTest {
        assertFailsWith<IllegalStateException> {
            kutex.accessor.value
        }
    }

    @Test
    fun accessing_value_while_locked_does_not_throw() = scope.runTest {
        kutex.lock()
        assertEquals(value, kutex.accessor.value)
    }
}
