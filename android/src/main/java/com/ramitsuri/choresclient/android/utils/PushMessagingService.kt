package com.ramitsuri.choresclient.android.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ramitsuri.choresclient.android.work.PushMessageTokenUploader
import com.ramitsuri.choresclient.utils.PushMessageProcessor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PushMessagingService : FirebaseMessagingService(), KoinComponent {
    private val pushMessageProcessor: PushMessageProcessor by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushMessageTokenUploader.upload(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Received push message: ${message.data}")
        pushMessageProcessor.onPushMessageReceived(data = message.data)
    }

    companion object {
        private const val TAG = "PushMessagingService"
    }
}