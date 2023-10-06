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
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.entities.AssignmentAlarm
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.notification.NotificationActionInfo
import com.ramitsuri.choresclient.notification.NotificationInfo
import com.ramitsuri.choresclient.notification.NotificationManager
import com.ramitsuri.choresclient.notification.Priority
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.toJavaDuration

class ShowNotificationWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), KoinComponent {

    private val logger: LogHelper by inject()
    private val notificationManager: NotificationManager by inject()
    private val assignmentRepo: TaskAssignmentsRepository by inject()
    private val alarmHandler: AlarmHandler by inject()
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

        val notificationId = alarmHandler.getExisting(assignmentId)?.systemNotificationId?.toInt()
        if (notificationId == null) {
            logger.v(TAG, "Notification Id is null. Cannot show notification")
            return failure()
        }

        val notificationActions = prefManager.getEnabledNotificationActions().map {
            val action = NotificationAction.fromAction(it)
            NotificationActionInfo(
                action.action,
                action.text,
                AssignmentActionReceiver::class
            )
        }
        notificationManager.showNotification(
            NotificationInfo(
                id = notificationId,
                channelId = applicationContext.getString(R.string.notification_reminders_id),
                priority = Priority.HIGH,
                title = assignment.taskName,
                body = null,
                additionalText = null,
                iconResId = R.drawable.ic_notification,
                actions = notificationActions,
                actionExtras = mapOf(
                    NotificationActionExtra.KEY_ASSIGNMENT_ID to assignmentId,
                )
            )
        )
        return success()
    }

    companion object {
        private const val WORK_TAG = "ReminderSchedulerWorker"
        private const val ASSIGNMENT_ID = "assignment_id"
        private const val TAG = "ShowNotification"

        fun schedule(context: Context, assignmentAlarms: List<AssignmentAlarm>) {
            val workManager = WorkManager.getInstance(context)
            assignmentAlarms.forEach { assignmentAlarm ->
                val workName = getWorkName(assignmentAlarm.assignmentId)

                val showAfter = assignmentAlarm
                    .showAtTime
                    .toInstant(TimeZone.currentSystemDefault())
                    .minus(Clock.System.now())

                val inputData = workDataOf(
                    ASSIGNMENT_ID to assignmentAlarm.assignmentId
                )

                val constraints = Constraints.Builder()
                    .setRequiresCharging(false)
                    .build()

                val builder = OneTimeWorkRequest
                    .Builder(ShowNotificationWorker::class.java)
                    .addTag(workName)
                    .setInitialDelay(showAfter.toJavaDuration())
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