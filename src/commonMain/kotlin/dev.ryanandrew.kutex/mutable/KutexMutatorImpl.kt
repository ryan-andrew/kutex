package dev.ryanandrew.kutex.mutable

internal class KutexMutatorImpl<T>(private val get: () -> T, private val set: (T) -> Unit) : KutexMutator<T> {
    override var value: T
        get() = get()
        set(value) = set(value)
}
