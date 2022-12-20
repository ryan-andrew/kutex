package dev.ryanandrew.kutex.common

import kotlinx.coroutines.sync.Mutex

/**
 * Abstract class that handles the common back-end [Mutex] implementation for implementations of [KutexInterface] like
 * [KutexImpl][dev.ryanandrew.kutex.immutable.KutexImpl] and
 * [MutableKutexImpl][dev.ryanandrew.kutex.mutable.MutableKutexImpl]
 */
internal abstract class AbstractKutex(
    private val mutex: Mutex = Mutex()
) {
    val isLocked: Boolean get() = mutex.isLocked
    fun tryLock() = mutex.tryLock()
    suspend fun lock() = mutex.lock()
    fun unlock() = mutex.unlock()
}
