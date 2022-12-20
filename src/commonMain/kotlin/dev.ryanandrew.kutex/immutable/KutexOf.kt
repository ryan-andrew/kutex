package dev.ryanandrew.kutex.immutable

/**
 * Create a new [Kutex] instance, assigning the [value] to it. This wraps the [value], allowing for coroutine-safe
 * access to it where needed. This is the same as [mutableKutexOf][dev.ryanandrew.kutex.mutable.mutableKutexOf], but
 * does not allow the [value] to be reassigned.
 */
fun <T> kutexOf(value: T): Kutex<T> = KutexImpl(value)
