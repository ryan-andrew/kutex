package dev.ryanandrew.kutex.common


/**
 * A Kutex provides safe access to a value across coroutines.
 *
 * - [Kutex][dev.ryanandrew.kutex.immutable.Kutex] is the default immutable version for which the value cannot be
 * reassigned.
 * - [MutableKutex][dev.ryanandrew.kutex.mutable.MutableKutex] is the mutable version for which the value
 * can be reassigned.
 *
 * The standard [Mutex][kotlinx.coroutines.sync.Mutex] is a more abstract locking and unlocking mechanism.
 *
 * If safe access to an object is needed across coroutines, use either a [Kutex][dev.ryanandrew.kutex.immutable.Kutex]
 * or [MutableKutex][dev.ryanandrew.kutex.mutable.MutableKutex].
 *
 * Kutex uses [Mutex][kotlinx.coroutines.sync.Mutex] under the hood, so inherits its properties.
 */
interface KutexInterface<T> {
    /**
     * Returns `true` when this Kutex is locked.
     */
    val isLocked: Boolean

    /**
     * Tries to lock this Kutex, returning `false` if this Kutex is already locked.
     */
    @Deprecated("Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock")
    fun tryLock(): Boolean

    /**
     * Locks this Kutex, suspending caller while the Kutex is locked.
     *
     * This suspending function is cancellable. If the [Job][kotlinx.coroutines.Job] of the current coroutine is
     * cancelled or completed while this function is suspended, this function immediately resumes with
     * [CancellationException][kotlinx.coroutines.CancellationException]. There is a **prompt cancellation guarantee**.
     * If the job was cancelled while this function was suspended, it will not resume successfully.
     * See [suspendCancellableCoroutine][kotlinx.coroutines.suspendCancellableCoroutine] documentation for low-level
     * details.
     * This function releases the lock if it was already acquired by this function before the
     * [CancellationException][kotlinx.coroutines.CancellationException] was thrown.
     *
     * Note that this function does not check for cancellation when it is not suspended.
     * Use [yield][kotlinx.coroutines.yield] or [CoroutineScope.isActive][kotlinx.coroutines.isActive] to periodically
     * check for cancellation in tight loops if needed.
     *
     * Use [tryLock] to try acquiring a lock without waiting.
     *
     * This function is fair; suspended callers are resumed in first-in-first-out order.
     */
    @Deprecated("Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock")
    suspend fun lock()

    /**
     * Unlocks this Kutex. Throws [IllegalStateException] if invoked on a kutex that is not locked or
     * was locked with a different owner token (by identity).
     */
    @Deprecated("Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock")
    fun unlock()
}