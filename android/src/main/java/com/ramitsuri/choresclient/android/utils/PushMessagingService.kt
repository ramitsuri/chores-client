package com.ramitsuri.choresclient.android.utils

import com.google.firebase.messaging.FirebaseMessagingService
import com.ramitsuri.choresclient.android.work.PushMessageTokenUploader
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PushMessagingService : FirebaseMessagingService(), KoinComponent {

    private val pushMessageTokenUploader: PushMessageTokenUploader.Companion by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pushMessageTokenUploader.upload(applicationContext)
    }
}