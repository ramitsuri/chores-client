package com.ramitsuri.choresclient.android.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Stub worker kept for backward compatibility.
 * If a user upgrades from a previous app version with pending ShowNotificationWorker jobs
 * enqueued in WorkManager, keeping this class prevents ClassNotFoundException crashes.
 */
class ShowNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return Result.success()
    }
}
