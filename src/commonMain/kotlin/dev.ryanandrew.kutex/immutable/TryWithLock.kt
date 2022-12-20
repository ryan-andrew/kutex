@file:OptIn(ExperimentalContracts::class)
@file:Suppress("DEPRECATION")

package dev.ryanandrew.kutex.immutable

import dev.ryanandrew.kutex.trylock.KutexTryLockResult
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * If the [Kutex] is not currently locked, executes the given [action] under this Kutex's lock. This returns a
 * [KutexTryLockResult], which can be used to check for success, get the return value of the [action], etc.
 * If the Kutex is already locked, the [action] will not be run, and the return value's
 * [wasAlreadyLocked][KutexTryLockResult.wasAlreadyLocked] field will be `true`. The [action]'s receiver is a
 * [KutexAccessor], giving the action access to the [value][KutexAccessor.value] held by the [Kutex]. This allows for
 * safe access to the value across coroutines.
 *
 * Ex:
 * ```kotlin
 * val currName = kutexOf("my name")
 * val name = currName.tryWithLock { value }.getOrElse { "default string for when currName is locked" }
 * val nullableName = currName.tryWithLock { value }.getOrNull()
 * val hasNameChanged = currName.tryWithLock { name != value }.getOrElse { false }
 * ```
 * Ex:
 * ```kotlin
 * someObjectKutex.tryWithLock {
 *     makeSomeTimeSensitiveCallUsing(value)
 * }.onAlreadyLocked {
 *     throw IllegalStateException("Kutex was locked!")
 * }
 * ```
 * @param action The action to be taken on the Kutex's [value][KutexAccessor.value]. We can read the value safely here.
 *
 * @return The return value of the [action].
 */
inline fun <R, T> Kutex<T>.tryWithLock(action: KutexAccessor<T>.() -> R): KutexTryLockResult<R> {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    return if (tryLock()) {
        try {
            KutexTryLockResult.success(action(accessor))
        } finally {
            unlock()
        }
    } else {
        KutexTryLockResult.failure()
    }
}
