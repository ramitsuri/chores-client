package com.ramitsuri.choresclient.android.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.model.TaskAssignment
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

        val notificationId = prefManager.getPreviousNotificationId() + 1
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
        prefManager.setPreviousNotificationId(notificationId)
        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "ReminderSchedulerWorker"
        private const val NOTIFICATION_BODY = "notification_body"

        fun schedule(context: Context, taskAssignment: TaskAssignment) {
            val workName = getWorkName(taskAssignment)
            Timber.d("Schedule $workName")

            val showAt = Duration.between(Instant.now(), taskAssignment.dueDateTime).seconds
            val inputData = workDataOf(NOTIFICATION_BODY to taskAssignment.task.name)

            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .build()

            val builder = OneTimeWorkRequest
                .Builder(ShowNotificationWorker::class.java)
                .addTag(workName)
                .setInitialDelay(showAt, TimeUnit.SECONDS)
                .setInputData(inputData)
                .setConstraints(constraints)

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.KEEP,
                    builder.build()
                )
        }

       fun cancel(context: Context, taskAssignment: TaskAssignment) {
            val workName = getWorkName(taskAssignment)
           Timber.d("Cancel $workName")

            WorkManager.getInstance(context)
                .cancelUniqueWork(workName)
        }

        private fun getWorkName(taskAssignment: TaskAssignment): String {
            return WORK_TAG + taskAssignment.id
        }
    }
}