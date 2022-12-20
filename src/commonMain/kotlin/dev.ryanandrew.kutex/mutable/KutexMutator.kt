package dev.ryanandrew.kutex.mutable

/**
 * Provides selective access to the mutable [value] held by a [MutableKutex].
 */
interface KutexMutator<T> {
    var value: T
}
