@file:OptIn(ExperimentalContracts::class)
@file:Suppress("DEPRECATION")

package dev.ryanandrew.kutex.immutable

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Executes the given [action] under this [Kutex]'s lock. The [action]'s receiver is a [KutexAccessor], giving the
 * action access to the value held by the [Kutex]. This allows for safe access to the value across coroutines.
 *
 * Ex:
 * ```kotlin
 * val currName = kutexOf("my name")
 * var name = currName.withLock { value }
 * name += "suffix"
 * val hasNameChanged = currName.withLock { name != value }
 * ```
 *
 * @param action The action to be taken on the kutex's value. We can read the value safely here.
 *
 * @return the return value of the action.
 */
suspend inline fun <R, T> Kutex<T>.withLock(
    action: KutexAccessor<T>.() -> R
): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    lock()
    try {
        return action(accessor)
    } finally {
        unlock()
    }
}
