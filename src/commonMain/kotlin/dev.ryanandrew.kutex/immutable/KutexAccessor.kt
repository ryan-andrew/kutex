package dev.ryanandrew.kutex.immutable

/**
 * Provides selective access to the final [value] held by a [Kutex].
 */
interface KutexAccessor<T> {
    /**
     * The read-only value held by a Kutex.
     */
    val value: T
}
