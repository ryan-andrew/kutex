package dev.ryanandrew.kutex.mutable

/**
 * Create a new [MutableKutex] instance, assigning it a [defaultValue]. This wraps the [defaultValue], allowing
 * for coroutine-safe access to it where needed. This is the same as [kutexOf][dev.ryanandrew.kutex.immutable.kutexOf],
 * but allows for the [defaultValue] to be reassigned safely.
 */
fun <T> mutableKutexOf(defaultValue: T): MutableKutex<T> = MutableKutexImpl(defaultValue)
