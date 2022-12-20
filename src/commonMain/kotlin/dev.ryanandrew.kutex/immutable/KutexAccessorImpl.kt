package dev.ryanandrew.kutex.immutable

internal class KutexAccessorImpl<T>(private val get: () -> T) : KutexAccessor<T> {
    override val value: T
        get() = get()
}
