package com.ramitsuri.choresclient.android.downloader

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.AppHelper
import java.util.concurrent.TimeUnit
import org.koin.core.component.inject
import org.koin.core.component.KoinComponent
import timber.log.Timber

class AssignmentsDownloader(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: TaskAssignmentsRepository by inject()
    private val appHelper: AppHelper by inject()

    override suspend fun doWork(): Result {
        if (appHelper.isWorkerRunning()) {
            Timber.d("A worker is already running, exit")
            return Result.failure()
        }
        appHelper.setWorkerRunning(true)
        try {
            repository.refresh()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            appHelper.setWorkerRunning(false)
        }
        Timber.d("Run complete")
        return Result.success()
    }

    companion object {
        private const val WORK_TAG = "AssignmentsDownloader"
        private const val REPEAT_HOURS: Long = 24

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
                    ExistingPeriodicWorkPolicy.REPLACE,
                    builder.build()
                )
        }
    }
}