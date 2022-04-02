package com.ramitsuri.choresclient.utils

import java.util.concurrent.locks.ReentrantLock

actual class Lock {
    private val mutex = ReentrantLock()

    actual fun lock() {
        mutex.lock()
    }
    actual fun unlock() {
        mutex.unlock()
    }
}
