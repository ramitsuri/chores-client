package com.ramitsuri.choresclient.utils

import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun getToken(logger: LogHelper, tag: String): String? {
    return suspendCoroutine { continuation ->
        try {
            val value = Tasks.await(FirebaseMessaging.getInstance().token, 30, TimeUnit.SECONDS)
            logger.d(tag, "Token: $value")
            continuation.resume(value)
        } catch (e: Exception) {
            logger.v(tag, "Exception when getting token: ${e.message}")
            continuation.resume(null)
        }
    }
}