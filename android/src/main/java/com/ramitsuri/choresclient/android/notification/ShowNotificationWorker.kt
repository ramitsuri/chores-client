package com.ramitsuri.choresclient.android.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker.Result.failure
import androidx.work.ListenableWorker.Result.success
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.utils.NotificationAction
import com.ramitsuri.choresclient.android.utils.NotificationActionExtra
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.entities.AssignmentAlarm
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.notification.NotificationActionInfo
import com.ramitsuri.choresclient.notification.NotificationHandler
import com.ramitsuri.choresclient.notification.NotificationInfo
import com.ramitsuri.choresclient.notification.Priority
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class ShowNotificationWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val logger: LogHelper by inject()
    private val notificationHandler: NotificationHandler by inject()
    private val assignmentRepo: TaskAssignmentsRepository by inject()
    private val prefManager: PrefManager by inject()

    override suspend fun doWork(): Result {
        val assignmentId = inputData.getString(ASSIGNMENT_ID)
        if (assignmentId == null) {
            logger.v(TAG, "Assignment Id null. Cannot show notification")
            return failure()
        }

        val assignment = assignmentRepo.getLocal(assignmentId)
        if (assignment == null) {
            logger.v(TAG, "Assignment null. Cannot show notification")
            return failure()
        }

        if (assignment.progressStatus != ProgressStatus.TODO) {
            logger.v(TAG, "Assignment not TODO. Skipping showing notification")
            return success()
        }

        val notificationTitle = inputData.getString(NOTIFICATION_BODY)
            ?: applicationContext.getString(R.string.notification_reminder_title)
        logger.d(TAG, "Showing notification for: $notificationTitle")

        val providedNotificationId = inputData.getInt(NOTIFICATION_ID, -1)
        val notificationId = if (providedNotificationId == -1) {
            // Provided Id cannot be used, generate a new one
            prefManager.generateNewNotificationId()
        } else {
            providedNotificationId
        }
        val notificationActions = prefManager.getEnabledNotificationActions().map {
            val action = NotificationAction.fromAction(it)
            NotificationActionInfo(
                action.action,
                action.text,
                AssignmentActionReceiver::class
            )
        }
        notificationHandler.buildAndShow(
            NotificationInfo(
                notificationId,
                applicationContext.getString(R.string.notification_reminders_id),
                Priority.HIGH,
                notificationTitle,
                null,
                R.drawable.ic_notification,
                notificationActions,
                mapOf(
                    NotificationActionExtra.KEY_ASSIGNMENT_ID to assignmentId,
                    NotificationActionExtra.KEY_NOTIFICATION_ID to notificationId,
                    NotificationActionExtra.KEY_NOTIFICATION_TEXT to notificationTitle
                )
            )
        )
        return success()
    }

    companion object {
        private const val WORK_TAG = "ReminderSchedulerWorker"
        private const val NOTIFICATION_BODY = "notification_body"
        private const val NOTIFICATION_ID = "notification_id"
        private const val ASSIGNMENT_ID = "assignment_id"
        private const val TAG = "ShowNotification"

        fun schedule(context: Context, assignmentAlarms: List<AssignmentAlarm>) {
            val workManager = WorkManager.getInstance(context)
            assignmentAlarms.forEach { assignmentAlarm ->
                val workName = getWorkName(assignmentAlarm.assignmentId)

                val showAfter =
                    assignmentAlarm.showAtTime.toInstant(TimeZone.currentSystemDefault())
                        .toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()
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
                workManager.cancelUniqueWork(workName)
            }
        }

        private fun getWorkName(assignmentId: String): String {
            return WORK_TAG + assignmentId
        }
    }
}