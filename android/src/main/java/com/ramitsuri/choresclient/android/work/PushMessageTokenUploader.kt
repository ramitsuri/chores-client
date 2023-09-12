package com.ramitsuri.choresclient.android.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.choresclient.model.error.EditTaskError
import com.ramitsuri.choresclient.model.error.Error
import com.ramitsuri.choresclient.model.error.PushTokenError
import com.ramitsuri.choresclient.repositories.PushMessageTokenRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.ramitsuri.choresclient.model.Result as InternalResult

class PushMessageTokenUploader(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: PushMessageTokenRepository by inject()

    override suspend fun doWork(): Result {
        return when (val submitResult = repository.submitToken()) {
            is InternalResult.Failure -> {
                when (submitResult.error) {
                    is PushTokenError.NotLoggedIn -> Result.failure()

                    is PushTokenError.NoDeviceId,
                    is PushTokenError.NoToken,
                    is Error.NoInternet,
                    is Error.Server,
                    is Error.Unknown -> {
                        Result.retry()
                    }

                    EditTaskError.TaskNotFound -> {
                        // Not possible here
                        Result.failure()
                    }
                }
            }

            is InternalResult.Success -> Result.success()
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