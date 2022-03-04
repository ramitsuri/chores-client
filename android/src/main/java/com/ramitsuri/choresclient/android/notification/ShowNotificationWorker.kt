package com.ramitsuri.choresclient.android.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.data.AssignmentAlarm
import com.ramitsuri.choresclient.android.utils.PrefManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

@HiltWorker
class ShowNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val notificationHandler: NotificationHandler,
    private val prefManager: PrefManager
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val notificationBody = inputData.getString(NOTIFICATION_BODY)
            ?: applicationContext.getString(R.string.notification_reminder_message)
        Timber.d("Showing notification for: $notificationBody")

        val providedNotificationId = inputData.getInt(NOTIFICATION_ID, -1)
        val notificationId = if (providedNotificationId == -1) {
            // Provided Id cannot be used, generate a new one
            prefManager.generateNewNotificationId()
        } else {
            providedNotificationId
        }
        notificationHandler.buildAndShow(
            NotificationInfo(
                notificationId,
                applicationContext.getString(R.string.notification_reminders_id),
                Priority.HIGH,
                R.string.notification_reminder_title,
                notificationBody,
                R.drawable.ic_notification
            )
        )
        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "ReminderSchedulerWorker"
        private const val NOTIFICATION_BODY = "notification_body"
        private const val NOTIFICATION_ID = "notification_id"

        fun schedule(context: Context, assignmentAlarms: List<AssignmentAlarm>) {
            val workManager = WorkManager.getInstance(context)
            assignmentAlarms.forEach { assignmentAlarm ->
                val workName = getWorkName(assignmentAlarm.assignmentId)
                Timber.d("Schedule $workName")

                val showAfter = Duration.between(Instant.now(), assignmentAlarm.showAtTime).seconds
                val inputData = workDataOf(
                    NOTIFICATION_BODY to assignmentAlarm.systemNotificationText,
                    NOTIFICATION_ID to assignmentAlarm.assignmentId
                )

                val constraints = Constraints.Builder()
                    .setRequiresCharging(false)
                    .build()

                val builder = OneTimeWorkRequest
                    .Builder(ShowNotificationWorker::class.java)
                    .addTag(workName)
                    .setInitialDelay(showAfter, TimeUnit.SECONDS)
                    .setInputData(inputData)
                    .setConstraints(constraints)

                workManager
                    .enqueueUniqueWork(
                        workName,
                        ExistingWorkPolicy.REPLACE,
                        builder.build()
                    )
            }
        }

        fun cancel(context: Context, assignmentIds: List<String>) {
            val workManager = WorkManager.getInstance(context)
            assignmentIds.forEach { assignmentId ->
                val workName = getWorkName(assignmentId)
                Timber.d("Cancel $workName")

                workManager.cancelUniqueWork(workName)
            }
        }

        private fun getWorkName(assignmentId: String): String {
            return WORK_TAG + assignmentId
        }
    }
}