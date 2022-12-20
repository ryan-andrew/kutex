@file:OptIn(ExperimentalContracts::class)
@file:Suppress("DEPRECATION")

package dev.ryanandrew.kutex.mutable

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Executes the given [action] under this kutex's lock. The [action]'s receiver is a [KutexMutator], giving the action
 * access to the value held by the [MutableKutex]. This allows for safe access to and reassignment of the value across
 * coroutines.
 *
 * Ex:
 * ```kotlin
 * val currName = mutableKutexOf("my name")
 * val name = currName.withLock { value }
 * currName.withLock { value = "my new name" }
 * val hasCurrNameChanged = currName.withLock { name != value }
 * ```
 * @param action The action to be taken on the kutex's value. We can read and reassign the value safely here.
 *
 * @return the return value of the action.
 */
suspend inline fun <R, T> MutableKutex<T>.withLock(action: KutexMutator<T>.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    lock()
    try {
        return action(mutator)
    } finally {
        unlock()
    }
}
