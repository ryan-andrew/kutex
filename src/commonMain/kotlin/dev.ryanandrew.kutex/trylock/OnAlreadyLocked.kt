package dev.ryanandrew.kutex.trylock

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Performs the given [action] if the kutex [wasAlreadyLocked][KutexTryLockResult.wasAlreadyLocked].
 * Returns the original [KutexTryLockResult] unchanged.
 *
 * Ex:
 * ```kotlin
 * kutex.tryWithLock {
 *     doSomethingWith(value)
 * }.onAlreadyLocked {
 *     println("was locked!")
 *     return
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> KutexTryLockResult<T>.onAlreadyLocked(action: () -> Unit): KutexTryLockResult<T> {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    if (!successfullyLocked) action()
    return this
}
