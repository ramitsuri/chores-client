package com.ramitsuri.choresclient.android.utils

import java.util.concurrent.atomic.AtomicBoolean

class AppHelper {

    private var isWorkerRunning = AtomicBoolean(false)

    fun setWorkerRunning(running: Boolean) {
        isWorkerRunning.set(running)
    }

    fun isWorkerRunning(): Boolean {
        return isWorkerRunning.get()
    }
}