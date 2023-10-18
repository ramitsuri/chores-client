package com.ramitsuri.choresclient.android.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.utils.NotificationId
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.ContentDownloader
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.time.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ContentDownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val contentDownloader: ContentDownloader by inject()

    override suspend fun doWork(): Result {
        logger.v(TAG, "Doing work")
        try {
            if (!isRunning.compareAndSet(false, true)) {
                logger.v(TAG, "Already running, will retry")
                return Result.retry()
            }
            val forceRemindPastDue = inputData.getBoolean(FORCE_REMIND_PAST_DUE_ARG, false)
            setProgress(workDataOf(RUNNING_STATUS to true))
            contentDownloader.download(
                now = Clock.System.now(),
                forceRemindPastDue = forceRemindPastDue
            )
            logger.v(TAG, "Run complete")
        } finally {
            isRunning.set(false)
            setProgress(workDataOf(RUNNING_STATUS to false))
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext, applicationContext.getString(
                R.string.notification_worker_id
            )
        ).apply {
            setSmallIcon(R.drawable.ic_notification)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentTitle(applicationContext.getString(R.string.notification_worker_title))
        }.build()
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    companion object : ContentDownloadRequestHandler, KoinComponent {
        private const val WORK_NAME_PERIODIC = "AssignmentsDownloader"
        private const val REPEAT_HOURS: Long = 4
        private const val WORK_NAME_ONE_TIME = "AssignmentsDownloader_OneTime"
        private const val TAG = "AssignmentsDownloader"
        private const val NOTIFICATION_ID = NotificationId.CONTENT_DOWNLOAD_FOREGROUND_WORKER
        private const val RUNNING_STATUS = "running_status"
        private const val FORCE_REMIND_PAST_DUE_ARG = "force_remind_past_due"

        private val isRunning = AtomicBoolean(false)

        private val logger: LogHelper by inject()

        fun enqueuePeriodic(context: Context) {
            val builder = PeriodicWorkRequest
                .Builder(ContentDownloadWorker::class.java, REPEAT_HOURS, TimeUnit.HOURS)
                .addTag(WORK_NAME_PERIODIC)
                .setConstraints(getConstraints())

            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME_PERIODIC,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    builder.build()
                )
        }

        override fun requestImmediateDownload(forceRemindPastDue: Boolean): Flow<Boolean> {
            logger.v(TAG, "Immediate download scheduled")
            return enqueue(get<Context>(), forceRemindPastDue = forceRemindPastDue, expedite = true)
        }

        override fun requestDelayedDownload(forceRemindPastDue: Boolean) {
            logger.v(TAG, "Delayed download scheduled")
            enqueue(get<Context>(), forceRemindPastDue = forceRemindPastDue, expedite = false)
        }

        private fun enqueue(
            context: Context,
            forceRemindPastDue: Boolean,
            expedite: Boolean = false,
        ): Flow<Boolean> {
            val inputData = workDataOf(
                FORCE_REMIND_PAST_DUE_ARG to forceRemindPastDue,
            )
            val builder = OneTimeWorkRequest
                .Builder(ContentDownloadWorker::class.java)
                .addTag(WORK_NAME_ONE_TIME)
                .setConstraints(getConstraints())
                .setInputData(inputData)
                .apply {
                    if (expedite) {
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    } else {
                        setInitialDelay(Duration.ofMinutes(1))
                    }
                }

            val workRequest = builder.build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME_ONE_TIME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )

            return WorkManager.getInstance(context).getWorkInfoByIdFlow(workRequest.id)
                .map { workInfo ->
                    workInfo?.progress?.getBoolean(RUNNING_STATUS, false) ?: false
                }
        }

        private fun getConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
        }
    }
}