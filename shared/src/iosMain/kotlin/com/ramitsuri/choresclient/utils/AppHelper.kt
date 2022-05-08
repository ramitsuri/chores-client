package com.ramitsuri.choresclient.utils

import kotlinx.atomicfu.atomic

actual class AppHelper {
    private var isRunning: Boolean by atomic(false)

    actual fun setWorkerRunning(running: Boolean) {
        isRunning = running
    }

    actual fun isWorkerRunning(): Boolean {
        return isRunning
    }
}