package com.ramitsuri.choresclient.utils

import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun getToken(): String? {
    return suspendCoroutine { continuation ->
        try {
            val value = Tasks.await(FirebaseMessaging.getInstance().token, 30, TimeUnit.SECONDS)
            continuation.resume(value)
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }
}