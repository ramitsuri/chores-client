package com.ramitsuri.choresclient.utils

expect class AppHelper {
    fun setWorkerRunning(running: Boolean)
    fun isWorkerRunning(): Boolean
}