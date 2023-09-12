package com.ramitsuri.choresclient.android.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.ramitsuri.choresclient.android.work.PushMessageTokenUploader

class PushMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushMessageTokenUploader.upload(applicationContext)
    }
}