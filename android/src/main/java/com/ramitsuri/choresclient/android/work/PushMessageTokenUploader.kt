package com.ramitsuri.choresclient.android.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.choresclient.repositories.PushMessageTokenRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PushMessageTokenUploader(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: PushMessageTokenRepository by inject()

    override suspend fun doWork(): Result {
        when (repository.submitToken()) {
            PushMessageTokenRepository.Outcome.NOT_LOGGED_IN,
            PushMessageTokenRepository.Outcome.EXCEPTION -> {
                return Result.failure()
            }

            PushMessageTokenRepository.Outcome.NO_TOKEN,
            PushMessageTokenRepository.Outcome.NO_DEVICE_ID,
            PushMessageTokenRepository.Outcome.UPLOAD_FAILED -> {
                return Result.retry()
            }

            PushMessageTokenRepository.Outcome.SUCCESS -> {
                return Result.success()
            }
        }
    }

    companion object {
        private const val WORK_NAME = "PushMessageTokenUploader"

        fun upload(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val builder = OneTimeWorkRequest
                .Builder(PushMessageTokenUploader::class.java)
                .addTag(WORK_NAME)
                .setConstraints(constraints)

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    builder.build()
                )
        }
    }
}