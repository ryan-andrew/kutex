@file:Suppress("UNCHECKED_CAST", "RedundantVisibilityModifier")
@file:OptIn(ExperimentalContracts::class)

package dev.ryanandrew.kutex.trylock

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Returns the [KutexTryLockResult]'s encapsulated value if the kutex was
 * [successfullyLocked][KutexTryLockResult.successfullyLocked] or the result of [onAlreadyLocked] function if it
 * [wasAlreadyLocked][KutexTryLockResult.wasAlreadyLocked].
 *
 * Ex:
 * ```kotlin
 * val x: String = myKutex.tryWithLock {
 *     value.toString()
 * }.getOrElse {
 *     "some default string for when lock could not be acquired"
 * }
 * ```
 */
inline fun <R, T : R> KutexTryLockResult<T>.getOrElse(onAlreadyLocked: () -> R): R {
    contract { callsInPlace(onAlreadyLocked, InvocationKind.AT_MOST_ONCE) }
    return if (successfullyLocked) { value as T } else { onAlreadyLocked() }
}
