package com.ramitsuri.choresclient.android.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimeZoneChangeReceiver : BroadcastReceiver(), KoinComponent {
    private val coroutineScope: CoroutineScope by inject()
    private val alarmHandler: AlarmHandler by inject()
    private val logger: LogHelper by inject()
    private val contentDownloadRequestHandler: ContentDownloadRequestHandler by inject()

    override fun onReceive(context: Context?, intent: Intent) {
        logger.v(TAG, "Restoring alarm and scheduling download after action: ${intent.action}")
        val pendingResult = goAsync()
        coroutineScope.launch {
            try {
                alarmHandler.onAlarmFired()
            } catch (e: Exception) {
                logger.v(TAG, "Error restoring alarm on timezone change: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
        // Time zone changed. ForceRemind so that notification alarms can be scheduled for the new time
        contentDownloadRequestHandler.requestDelayedDownload(
            forceRemindPastDue = true,
            forceRemindFuture = true,
        )
    }

    companion object {
        private const val TAG = "TimeZoneChangeReceiver"
    }
}
