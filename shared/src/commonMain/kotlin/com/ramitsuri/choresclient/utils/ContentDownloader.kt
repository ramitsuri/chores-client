package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class ContentDownloader(
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val syncRepository: SyncRepository,
    private val prefManager: PrefManager,
    private val isDebug: Boolean
) : KoinComponent {

    private val logger: LogHelper by inject()
    suspend fun download(
        now: Instant,
        forceDownload: Boolean = false,
        forceRemindPastDue: Boolean = false,
    ) {
        try {
            val shouldDownloadSyncStuff = isDebug
                .or(forceDownload)
                .or(lastSyncThresholdReached(now))
            if (shouldDownloadSyncStuff) {
                syncRepository.refresh()
            }

            taskAssignmentsRepository.refresh(
                forceRemindPastDue = forceRemindPastDue
            )
            prefManager.setLastSyncTime()
        } catch (e: Exception) {
            logger.v(TAG, e.message ?: e.toString())
        }
    }

    private fun lastSyncThresholdReached(now: Instant): Boolean {
        return now.minus(prefManager.getLastSyncTime()) > syncMinDuration
    }

    companion object {
        private const val TAG = "ContentDownloader"
        private val syncMinDuration: Duration = 24.hours
    }
}