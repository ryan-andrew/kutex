package dev.ryanandrew.kutex.trylock

import kotlin.test.*

class KutexTryLockResultTest {
    @Test
    fun successful_result_returns_value_for_getOrNull() {
        val kutexValue = "value"
        val success = KutexTryLockResult.success(kutexValue)
        assertEquals(kutexValue, success.getOrNull())
    }

    @Test
    fun failure_result_returns_null_for_getOrNull() {
        val failure = KutexTryLockResult.failure<String>()
        assertNull(failure.getOrNull())
    }

    @Test
    fun wasAlreadyLocked_always_true_for_failure() {
        val failure = KutexTryLockResult.failure<String>()
        assertTrue(failure.wasAlreadyLocked)
    }

    @Test
    fun successfullyLocked_never_true_for_failure() {
        val failure = KutexTryLockResult.failure<String>()
        assertFalse(failure.successfullyLocked)
    }

    @Test
    fun wasAlreadyLocked_never_true_for_success() {
        val success = KutexTryLockResult.success("value")
        assertFalse(success.wasAlreadyLocked)
    }

    @Test
    fun successfullyLocked_always_true_for_success() {
        val success = KutexTryLockResult.success("value")
        assertTrue(success.successfullyLocked)
    }

    @Test
    fun onAlreadyLocked_never_called_for_successful_result() {
        var didRun = false
        KutexTryLockResult.success("value").onAlreadyLocked {
            didRun = true
        }
        assertFalse(didRun)
    }

    @Test
    fun onAlreadyLocked_always_called_for_failure_result() {
        var didRun = false
        KutexTryLockResult.failure<String>().onAlreadyLocked {
            didRun = true
        }
        assertTrue(didRun)
    }

    @Test
    fun getOrElse_never_called_for_successful_result() {
        var didRun = false
        KutexTryLockResult.success("value").getOrElse {
            didRun = true
        }
        assertFalse(didRun)
    }

    @Test
    fun getOrElse_always_called_for_failure_result() {
        var didRun = false
        KutexTryLockResult.failure<String>().getOrElse {
            didRun = true
        }
        assertTrue(didRun)
    }

    @Test
    fun getOrElse_always_returns_value_for_successful_result() {
        val expected = "value"
        val actual = KutexTryLockResult.success(expected).getOrElse {
            "some other value"
        }
        assertEquals(expected, actual)
    }

    @Test
    fun getOrElse_always_returns_else_lambda_result_for_failure_result() {
        val expected = "value"
        val actual = KutexTryLockResult.failure<String>().getOrElse {
            expected
        }
        assertEquals(expected, actual)
    }

    @Test
    fun toString_contains_value_for_successful_result() {
        val value = "some kutex value"
        assertTrue(value in KutexTryLockResult.success(value).toString())
    }

    @Test
    fun toString_contains_null_for_failure_result() {
        assertTrue("${null}" in KutexTryLockResult.failure<String>().toString())
    }

    @Test
    fun toString_contains_success_boolean_for_successful_result() {
        assertTrue(true.toString() in KutexTryLockResult.success("any value").toString())
    }

    @Test
    fun toString_contains_success_boolean_for_failure_result() {
        assertTrue(false.toString() in KutexTryLockResult.failure<String>().toString())
    }
}
