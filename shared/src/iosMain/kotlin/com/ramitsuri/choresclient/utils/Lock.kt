package com.ramitsuri.choresclient.utils

import kotlinx.cinterop.cValue
import utils.ktor_mutex_create
import utils.ktor_mutex_lock
import utils.ktor_mutex_t
import utils.ktor_mutex_unlock

actual class Lock {
    private val mutex = cValue<ktor_mutex_t>()

    init {
        ktor_mutex_create(mutex)
    }

    actual fun lock() {
        ktor_mutex_lock(mutex)
    }

    actual fun unlock() {
        ktor_mutex_unlock(mutex)
    }
}