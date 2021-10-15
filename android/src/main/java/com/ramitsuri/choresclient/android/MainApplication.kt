package com.ramitsuri.choresclient.android

import android.app.Application
import com.ramitsuri.choresclient.android.notification.Importance
import com.ramitsuri.choresclient.android.notification.NotificationChannelInfo
import com.ramitsuri.choresclient.android.notification.NotificationHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication: Application() {

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onCreate() {
        super.onCreate()

        notificationHandler.createChannels(
            listOf(
                NotificationChannelInfo(
                    id = getString(R.string.notification_reminders_id),
                    name = getString(R.string.notification_reminders_name),
                    description = getString(R.string.notification_reminders_description),
                    Importance.HIGH
                )
            )
        )
    }
}