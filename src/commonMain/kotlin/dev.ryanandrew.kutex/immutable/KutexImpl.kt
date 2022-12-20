@file:Suppress("OVERRIDE_DEPRECATION")

package dev.ryanandrew.kutex.immutable

import dev.ryanandrew.kutex.common.AbstractKutex

internal class KutexImpl<T> (value: T) : Kutex<T>, AbstractKutex() {
    override val accessor: KutexAccessor<T> = KutexAccessorImpl {
        check(isLocked) { "The accessor can only be used while the kutex is locked!" }
        value
    }
}
