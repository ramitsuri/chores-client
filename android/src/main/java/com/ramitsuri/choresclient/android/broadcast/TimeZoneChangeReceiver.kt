package com.ramitsuri.choresclient.android.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.LogHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimeZoneChangeReceiver : BroadcastReceiver(), KoinComponent {
    private val logger: LogHelper by inject()
    private val contentDownloadRequestHandler: ContentDownloadRequestHandler by inject()
    override fun onReceive(context: Context?, intent: Intent) {
        logger.v(TAG, "Scheduling download after action: ${intent.action}")
        // Time zone changed. ForceRemind so that notifications alarms can be scheduled for the new
        // time
        contentDownloadRequestHandler.requestDelayedDownload(
            forceRemindPastDue = true,
            forceRemindFuture = true,
        )
    }

    companion object {
        private const val TAG = "TimeZoneChangeReceiver"
    }
}