package com.ramitsuri.choresclient.android.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.utils.NotificationId
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.SyncStatus
import com.ramitsuri.choresclient.utils.TaskUploadRequestHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

class TaskUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val tasksRepository: TasksRepository by inject()

    override suspend fun doWork(): Result {
        logger.v(TAG, "Doing upload work")
        try {
            if (!isRunning.compareAndSet(false, true)) {
                logger.v(TAG, "Upload already running")
                return Result.retry()
            }
            setProgress(workDataOf(RUNNING_STATUS to true))
            tasksRepository.uploadLocal()
            logger.v(TAG, "Upload run complete")
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

    companion object : TaskUploadRequestHandler, KoinComponent {
        private const val WORK_NAME_UPLOAD = "TaskUploadWorker_OneTime"
        private const val TAG = "TaskUploadWorker"
        private const val NOTIFICATION_ID = NotificationId.TASK_UPLOAD_FOREGROUND_WORKER
        private const val RUNNING_STATUS = "running_status"

        private val isRunning = AtomicBoolean(false)
        private val logger: LogHelper by inject()

        override fun requestImmediateUpload(): Flow<Boolean> {
            logger.v(TAG, "Immediate task upload scheduled")
            return enqueue(get<Context>(), expedite = true)
        }

        override fun requestDelayedUpload() {
            logger.v(TAG, "Delayed task upload scheduled")
            enqueue(get<Context>(), expedite = false)
        }

        override fun getUploadStatusFlow(): Flow<SyncStatus> {
            val context = get<Context>()
            return WorkManager.getInstance(context)
                .getWorkInfosByTagFlow(WORK_NAME_UPLOAD)
                .map { workInfos ->
                    val isUploading = workInfos.any {
                        it.state == WorkInfo.State.RUNNING ||
                            it.progress.getBoolean(RUNNING_STATUS, false)
                    }
                    val isScheduled = workInfos.any {
                        it.state == WorkInfo.State.ENQUEUED
                    }
                    SyncStatus(
                        isUploading = isUploading,
                        isScheduled = isScheduled,
                    )
                }
        }

        private fun enqueue(context: Context, expedite: Boolean): Flow<Boolean> {
            val builder = OneTimeWorkRequest
                .Builder(TaskUploadWorker::class.java)
                .addTag(WORK_NAME_UPLOAD)
                .setConstraints(getConstraints())
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
                    WORK_NAME_UPLOAD,
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
