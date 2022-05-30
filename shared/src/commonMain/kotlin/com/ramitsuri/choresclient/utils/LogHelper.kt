package com.ramitsuri.choresclient.utils

expect class LogHelper {
    fun enableRemoteLogging(enable: Boolean)
    fun d(tag: String, message: String)
    fun v(tag: String, message: String)
}