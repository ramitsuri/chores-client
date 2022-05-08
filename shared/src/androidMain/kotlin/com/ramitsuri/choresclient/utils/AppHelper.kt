package com.ramitsuri.choresclient.utils

import java.util.concurrent.atomic.AtomicBoolean

actual class AppHelper {
    private val isRunning = AtomicBoolean(false)

    actual fun setWorkerRunning(running: Boolean) {
        isRunning.set(running)
    }

    actual fun isWorkerRunning(): Boolean {
        return isRunning.get()
    }
}