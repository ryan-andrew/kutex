package dev.ryanandrew.kutex.immutable

import dev.ryanandrew.kutex.common.KutexInterface

/**
 * A Kutex provides safe access to a value across coroutines. [Kutex] is the immutable version of
 * [MutableKutex][dev.ryanandrew.kutex.mutable.MutableKutex]. This means a [KutexAccessor] is provided, which can access
 * a wrapped [value][KutexAccessor.value], but not reassign it.
 *
 * **Instead of manually using low-level locking mechanisms, prefer to use [withLock] and [tryWithLock].**
 *
 * The standard [Mutex][kotlinx.coroutines.sync.Mutex] is a more abstract locking and unlocking mechanism.
 * If safe access to an object is needed across coroutines, use either a [Kutex][dev.ryanandrew.kutex.immutable.Kutex]
 * or [MutableKutex][dev.ryanandrew.kutex.mutable.MutableKutex].
 *
 * [Kutex] uses [Mutex][kotlinx.coroutines.sync.Mutex] under the hood, so inherits its properties.
 *
 * @see withLock
 * @see tryWithLock
 * @see dev.ryanandrew.kutex.mutable.MutableKutex
 */
interface Kutex<T> : KutexInterface<T> {
    /**
     * Provides selective access to read the [value][KutexAccessor.value] held by a [Kutex].
     */
    @Deprecated(
        message = "Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock",
        replaceWith = ReplaceWith("withLock(owner) {  }", "dev.ryanandrew.kutex.immutable.withLock")
    )
    val accessor: KutexAccessor<T>

    @Deprecated(
        message = "Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock",
        replaceWith = ReplaceWith("tryWithLock(owner) {  }", "dev.ryanandrew.kutex.immutable.tryWithLock")
    )
    override fun tryLock(): Boolean

    @Deprecated(
        message = "Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock",
        replaceWith = ReplaceWith("withLock(owner) {  }", "dev.ryanandrew.kutex.immutable.withLock")
    )
    override suspend fun lock()
}
