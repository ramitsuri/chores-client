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
import com.ramitsuri.choresclient.android.utils.NotificationAction
import com.ramitsuri.choresclient.android.utils.NotificationActionExtra
import com.ramitsuri.choresclient.data.entities.AssignmentAlarm
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.notification.NotificationActionInfo
import com.ramitsuri.choresclient.notification.NotificationHandler
import com.ramitsuri.choresclient.notification.NotificationInfo
import com.ramitsuri.choresclient.notification.Priority
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.datetime.Clock
import timber.log.Timber

@HiltWorker
class ShowNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val notificationHandler: NotificationHandler,
    private val prefManager: PrefManager
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val notificationTitle = inputData.getString(NOTIFICATION_BODY)
            ?: applicationContext.getString(R.string.notification_reminder_title)
        Timber.d("Showing notification for: $notificationTitle")

        val providedNotificationId = inputData.getInt(NOTIFICATION_ID, -1)
        val notificationId = if (providedNotificationId == -1) {
            // Provided Id cannot be used, generate a new one
            prefManager.generateNewNotificationId()
        } else {
            providedNotificationId
        }
        val assignmentId = inputData.getString(ASSIGNMENT_ID) ?: ""
        notificationHandler.buildAndShow(
            NotificationInfo(
                notificationId,
                applicationContext.getString(R.string.notification_reminders_id),
                Priority.HIGH,
                notificationTitle,
                null,
                R.drawable.ic_notification,
                listOf(
                    NotificationActionInfo(
                        NotificationAction.SNOOZE_HOUR.action,
                        NotificationAction.SNOOZE_HOUR.text,
                        AssignmentActionReceiver::class
                    ),
                    NotificationActionInfo(
                        NotificationAction.SNOOZE_DAY.action,
                        NotificationAction.SNOOZE_DAY.text,
                        AssignmentActionReceiver::class
                    ),
                    NotificationActionInfo(
                        NotificationAction.COMPLETE.action,
                        NotificationAction.COMPLETE.text,
                        AssignmentActionReceiver::class
                    )
                ),
                mapOf(
                    NotificationActionExtra.KEY_ASSIGNMENT_ID to assignmentId,
                    NotificationActionExtra.KEY_NOTIFICATION_ID to notificationId,
                    NotificationActionExtra.KEY_NOTIFICATION_TEXT to notificationTitle
                )
            )
        )
        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "ReminderSchedulerWorker"
        private const val NOTIFICATION_BODY = "notification_body"
        private const val NOTIFICATION_ID = "notification_id"
        private const val ASSIGNMENT_ID = "assignment_id"

        fun schedule(context: Context, assignmentAlarms: List<AssignmentAlarm>) {
            val workManager = WorkManager.getInstance(context)
            assignmentAlarms.forEach { assignmentAlarm ->
                val workName = getWorkName(assignmentAlarm.assignmentId)
                Timber.d("Schedule $workName")

                val showAfter =
                    assignmentAlarm.showAtTime.toEpochMilliseconds() - Clock.System.now()
                        .toEpochMilliseconds()
                val inputData = workDataOf(
                    NOTIFICATION_BODY to assignmentAlarm.systemNotificationText,
                    NOTIFICATION_ID to assignmentAlarm.systemNotificationId,
                    ASSIGNMENT_ID to assignmentAlarm.assignmentId
                )

                val constraints = Constraints.Builder()
                    .setRequiresCharging(false)
                    .build()

                val builder = OneTimeWorkRequest
                    .Builder(ShowNotificationWorker::class.java)
                    .addTag(workName)
                    .setInitialDelay(showAfter, TimeUnit.MILLISECONDS)
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