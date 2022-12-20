@file:Suppress("OVERRIDE_DEPRECATION")

package dev.ryanandrew.kutex.mutable

import dev.ryanandrew.kutex.common.AbstractKutex

internal class MutableKutexImpl<T>(defaultValue: T) : MutableKutex<T>, AbstractKutex() {
    var value: T = defaultValue

    override val mutator: KutexMutator<T> = KutexMutatorImpl(
        get = {
            checkLocked()
            value
        },
        set = {
            checkLocked()
            value = it
        },
    )

    private fun checkLocked() = check(isLocked) { "The mutator can only be used while the kutex is locked!" }
}
