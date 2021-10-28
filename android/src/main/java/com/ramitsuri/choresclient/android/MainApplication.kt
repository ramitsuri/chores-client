package com.ramitsuri.choresclient.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ramitsuri.choresclient.android.downloader.AssignmentsDownloader
import com.ramitsuri.choresclient.android.notification.Importance
import com.ramitsuri.choresclient.android.notification.NotificationChannelInfo
import com.ramitsuri.choresclient.android.notification.NotificationHandler
import com.ramitsuri.choresclient.android.reminder.ReminderSchedulerWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MainApplication: Application(), Configuration.Provider {

    @Inject
    lateinit var notificationHandler: NotificationHandler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        createNotificationChannels()
        enqueueWorkers()
    }

    private fun createNotificationChannels() {
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

    private fun enqueueWorkers() {
        AssignmentsDownloader.enqueuePeriodic(this)
        ReminderSchedulerWorker.enqueuePeriodic(this)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }
}