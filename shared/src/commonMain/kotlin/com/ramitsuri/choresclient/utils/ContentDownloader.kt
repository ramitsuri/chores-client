package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TasksRepository
import kotlin.time.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class ContentDownloader(
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val tasksRepository: TasksRepository,
    private val syncRepository: SyncRepository,
    private val prefManager: PrefManager,
    private val isDebug: Boolean
) : KoinComponent {

    private val logger: LogHelper by inject()
    suspend fun download(
        now: Instant,
        forceDownload: Boolean = false,
        forceRemindPastDue: Boolean = false,
        forceRemindFuture: Boolean = false,
    ) {
        try {
            val shouldDownloadSyncStuff = isDebug
                .or(forceDownload)
                .or(lastSyncThresholdReached(now))
            if (shouldDownloadSyncStuff) {
                syncRepository.refresh()
                tasksRepository.fetchTasks()
                prefManager.setLastSyncTime()
            }

            val shouldFetchCompleted = isDebug
                .or(forceDownload)
                .or(lastCompletedFetchThresholdReached(now))
            if (shouldFetchCompleted) {
                taskAssignmentsRepository.deleteOldCompleted(now)
                taskAssignmentsRepository.fetchAndSaveCompleted()
                prefManager.setLastCompletedFetchTime()
            }

            taskAssignmentsRepository.refresh(
                forceRemindPastDue = forceRemindPastDue,
                forceRemindFuture = forceRemindFuture,
            )
        } catch (e: Exception) {
            logger.v(TAG, e.message ?: e.toString())
        }
    }

    private fun lastSyncThresholdReached(now: Instant): Boolean {
        return now.minus(prefManager.getLastSyncTime()) > syncMinDuration
    }

    private fun lastCompletedFetchThresholdReached(now: Instant): Boolean {
        return now.minus(prefManager.getLastCompletedFetchTime()) > completedFetchMinDuration
    }

    companion object {
        private const val TAG = "ContentDownloader"
        private val syncMinDuration: Duration = 7.days
        private val completedFetchMinDuration: Duration = 24.hours
    }
}