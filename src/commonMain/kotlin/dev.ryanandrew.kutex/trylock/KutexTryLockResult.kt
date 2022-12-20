package dev.ryanandrew.kutex.trylock

/**
 * A result from an attempt to acquire a lock via [Kutex.tryWithLock][dev.ryanandrew.kutex.immutable.tryWithLock] or
 * [MutableKutex.tryWithLock][dev.ryanandrew.kutex.mutable.tryWithLock].
 *
 * If the Kutex was unlocked the instant the
 * attempt was made, [successfullyLocked] will be `true`, and the value stored by the Kutex will be available via
 * [getOrNull] or [getOrElse][dev.ryanandrew.kutex.trylock.getOrElse].
 *
 * Otherwise, [wasAlreadyLocked] will be true, and the value will be unavailable. All
 * [onAlreadyLocked][dev.ryanandrew.kutex.trylock.onAlreadyLocked] blocks will be executed.
 *
 * @see dev.ryanandrew.kutex.immutable.tryWithLock
 * @see dev.ryanandrew.kutex.mutable.tryWithLock
 * @see onAlreadyLocked
 * @see getOrElse
 */
class KutexTryLockResult<T> @PublishedApi internal constructor(
    @PublishedApi internal val value: T?,
    val successfullyLocked: Boolean
) {

    /**
     * Returns `true` if the kutex was already locked when the attempt to lock was made.
     * In this case [successfullyLocked] returns `false`.
     *
     * When this is `true`, [onAlreadyLocked] blocks will be executed.
     */
    val wasAlreadyLocked: Boolean get() = !successfullyLocked

    /**
     * Returns the kutex's encapsulated value if the kutex was [successfullyLocked] or `null` if it [wasAlreadyLocked].
     *
     * This function is a shorthand for `getOrElse { null }` (see [getOrElse]).
     *
     * Ex:
     * ```kotlin
     * val x: String? = myKutex.tryWithLock {
     *     value.toString()
     * }.getOrNull()
     * ```
     */
    fun getOrNull(): T? = value

    override fun toString(): String = "KutexTryLockResult(isSuccess=$successfullyLocked, value=$value)"

    companion object {
        /**
         * Instantiates a new [KutexTryLockResult] for when a Kutex was unlocked, the lock was acquired, and
         * the value of the Kutex is available.
         */
        @PublishedApi
        internal fun <T> success(value: T): KutexTryLockResult<T> = KutexTryLockResult(value, true)

        /**
         * Instantiates a new [KutexTryLockResult] for when a Kutex was already locked, the lock was not
         * acquired, and the value of the Kutex is not available.
         */
        @PublishedApi
        internal fun <T> failure(): KutexTryLockResult<T> = KutexTryLockResult(null, false)
    }
}
