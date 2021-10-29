package com.ramitsuri.choresclient.android.downloader

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.PrefManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class AssignmentsDownloader @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager
):
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (prefManager.isWorkerRunning()) {
            Timber.d("A worker is already running, exit")
            return Result.failure()
        }
        prefManager.setWorkerRunning(true)
        try {
            repository.getTaskAssignments()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            prefManager.setWorkerRunning(false)
        }
        Timber.d("Run complete")
        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "AssignmentsDownloader"
        private const val REPEAT_HOURS: Long = 6

        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val builder = PeriodicWorkRequest
                .Builder(AssignmentsDownloader::class.java, REPEAT_HOURS, TimeUnit.HOURS)
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