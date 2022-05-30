package com.ramitsuri.choresclient.android.reminder

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.choresclient.data.notification.ReminderScheduler
import com.ramitsuri.choresclient.utils.AppHelper
import com.ramitsuri.choresclient.utils.LogHelper
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderSchedulerWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val logger: LogHelper by inject()
    private val reminderScheduler: ReminderScheduler by inject()
    private val prefManager: AppHelper by inject()

    override suspend fun doWork(): Result {
        if (prefManager.isWorkerRunning()) {
            logger.v(TAG, "A worker is already running, exit")
            return Result.failure()
        }
        prefManager.setWorkerRunning(true)
        try {
            withContext(Dispatchers.IO) {
                reminderScheduler.addReminders()
            }
        } catch (e: Exception) {
            logger.v(TAG, e.message ?: e.toString())
        } finally {
            prefManager.setWorkerRunning(false)
        }
        logger.v(TAG, "Run complete")
        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "ReminderSchedulerWorker"
        private const val REPEAT_HOURS: Long = 2
        private const val TAG = "ReminderScheduler"

        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .build()

            val builder = PeriodicWorkRequest
                .Builder(ReminderSchedulerWorker::class.java, REPEAT_HOURS, TimeUnit.HOURS)
                .addTag(WORK_TAG)
                .setConstraints(constraints)

            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    builder.build()
                )
        }
    }
}