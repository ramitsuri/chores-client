package com.ramitsuri.choresclient.android.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.notification.ReminderScheduler
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.PrefManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderSchedulerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val reminderScheduler: ReminderScheduler,
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager
):
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        if (prefManager.isWorkerRunning()) {
            Timber.d("A worker is already running, exit")
            return Result.failure()
        }
        prefManager.setWorkerRunning(true)
        withContext(Dispatchers.IO) {
            val assignments = repository.getTaskAssignments(true)
            if (assignments is com.ramitsuri.choresclient.android.model.Result.Success) {
                val assignmentsForUser =
                    getAssignmentsForReminders(assignments.data, prefManager.getUserId())
                reminderScheduler.addReminders(assignmentsForUser)
            }
        }
        prefManager.setWorkerRunning(false)
        Timber.d("Run complete")
        return Result.success()
    }

    private fun getAssignmentsForReminders(
        data: List<TaskAssignment>,
        userId: String?
    ): List<TaskAssignment> {
        return data
            .filter {it.member.id == userId}
            .filter {it.progressStatus == ProgressStatus.TODO}
            .filter {it.task.repeatUnit != RepeatUnit.ON_COMPLETE}
    }

    companion object {
        private const val WORK_TAG = "ReminderSchedulerWorker"
        private const val REPEAT_HOURS: Long = 2

        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .build()

            val builder = PeriodicWorkRequest
                .Builder(ReminderSchedulerWorker::class.java, REPEAT_HOURS, TimeUnit.HOURS)
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