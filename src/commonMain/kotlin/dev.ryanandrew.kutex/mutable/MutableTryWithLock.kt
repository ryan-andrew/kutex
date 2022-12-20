@file:OptIn(ExperimentalContracts::class)
@file:Suppress("DEPRECATION")

package dev.ryanandrew.kutex.mutable

import dev.ryanandrew.kutex.trylock.KutexTryLockResult
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * If the [MutableKutex] is not currently locked, executes the given [action] under this Kutex's lock. This returns a
 * [KutexTryLockResult], which can be used to check for success, get the return value of the [action], etc.
 * If the Kutex is already locked, the [action] will not be run, and the return value's
 * [wasAlreadyLocked][KutexTryLockResult.wasAlreadyLocked] field will be `true`. The [action]'s receiver is a
 * [KutexMutator], giving the action access to the [value][KutexMutator.value] held by the [MutableKutex]. This allows
 * for safe access to the value and reassignment of the value across coroutines.
 *
 * Ex:
 * ```kotlin
 * val currName = mutableKutexOf("my name")
 * val name = currName.tryWithLock { value }.getOrElse { "default string for when currName is locked" }
 * val nullableName = currName.tryWithLock { value }.getOrNull()
 * currName.tryWithLock { value = "new name" }
 * val hasNameChanged = currName.tryWithLock { name != value }.getOrElse { false }
 * ```
 * Ex:
 * ```kotlin
 * someObjectKutex.tryWithLock {
 *     value = value.copy()
 *     makeSomeTimeSensitiveCallUsing(value)
 * }.onAlreadyLocked {
 *     throw IllegalStateException("Kutex was locked!")
 * }
 * ```
 * @param action The action to be taken on the Kutex's [value][KutexMutator.value]. We can read and reassign the value
 *        safely here.
 *
 * @return The return value of the [action].
 */
inline fun <R, T> MutableKutex<T>.tryWithLock(action: KutexMutator<T>.() -> R): KutexTryLockResult<R> {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    return if (tryLock()) {
        try {
            KutexTryLockResult.success(action(mutator))
        } finally {
            unlock()
        }
    } else {
        KutexTryLockResult.failure()
    }
}
