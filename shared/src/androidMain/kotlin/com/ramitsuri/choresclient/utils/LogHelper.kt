package com.ramitsuri.choresclient.utils

import co.touchlab.kermit.Logger
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ramitsuri.choresclient.BuildConfig

actual class LogHelper(
    private val enableLocal: Boolean,
    private var enableRemote: Boolean,
    private val deviceDetails: String
) {

    actual fun enableRemoteLogging(enable: Boolean) {
        localLog(message = "EnableRemote -> $enable")
        enableRemote = enable
    }

    actual fun d(tag: String, message: String) {
        localLog(tag, message)
    }

    actual fun v(tag: String, message: String) {
        localLog(tag, message)
        remoteLog(tag, message)
    }

    private fun localLog(tag: String = TAG, message: String) {
        if (enableLocal) {
            val logger = Logger.withTag(tag)
            logger.d(message)
        }
    }

    private fun remoteLog(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            return
        }
        if (enableRemote) {
            val remoteLog = "${formatLogTime()} $tag: $message"
            getDb().push().setValue(remoteLog)
        }
    }

    private fun getDb(): DatabaseReference {
        return Firebase.database.getReference("logs/$deviceDetails/${formatLogParent()}")
    }

    companion object {
        private const val TAG = "LogHelper"
    }
}