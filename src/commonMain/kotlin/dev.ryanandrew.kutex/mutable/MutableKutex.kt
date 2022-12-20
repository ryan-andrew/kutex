package dev.ryanandrew.kutex.mutable

import dev.ryanandrew.kutex.common.KutexInterface

/**
 * A Kutex provides safe access to a value across coroutines. [MutableKutex] is the mutable version of
 * [Kutex][dev.ryanandrew.kutex.immutable.Kutex]. This means a [KutexMutator] is provided, which can access
 * a wrapped [value][KutexMutator.value] and allows for its reassignment.
 *
 * **Instead of manually using low-level locking mechanisms, prefer to use [withLock] and [tryWithLock].**
 *
 * The standard [Mutex][kotlinx.coroutines.sync.Mutex] is a more abstract locking and unlocking mechanism.
 * If safe access to an object is needed across coroutines, use either a [Kutex][dev.ryanandrew.kutex.immutable.Kutex]
 * or [MutableKutex][dev.ryanandrew.kutex.mutable.MutableKutex].
 *
 * [MutableKutex] uses [Mutex][kotlinx.coroutines.sync.Mutex] under the hood, so inherits its properties.
 *
 * @see withLock
 * @see tryWithLock
 * @see dev.ryanandrew.kutex.mutable.MutableKutex
 */
interface MutableKutex<T> : KutexInterface<T> {
    /**
     * Provides selective access to the mutable [value][KutexMutator.value] held by a [MutableKutex].
     */
    @Deprecated(
        message = "Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock",
        replaceWith = ReplaceWith("withLock(owner) {  }", "dev.ryanandrew.kutex.mutable.withLock")
    )
    val mutator: KutexMutator<T>

    @Deprecated(
        message = "Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock",
        replaceWith = ReplaceWith("tryWithLock(owner) {  }", "dev.ryanandrew.kutex.mutable.tryWithLock")
    )
    override fun tryLock(): Boolean

    @Deprecated(
        message = "Manual locking mechanisms are discouraged. Instead, prefer to use withLock or tryWithLock",
        replaceWith = ReplaceWith("withLock(owner) {  }", "dev.ryanandrew.kutex.mutable.withLock")
    )
    override suspend fun lock()
}

