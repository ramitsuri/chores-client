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

class RestartBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val coroutineScope: CoroutineScope by inject()
    private val alarmHandler: AlarmHandler by inject()
    private val logger: LogHelper by inject()
    private val contentDownloadRequestHandler: ContentDownloadRequestHandler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (!(intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                    intent.action == Intent.ACTION_BOOT_COMPLETED ||
                    intent.action == Intent.ACTION_REBOOT ||
                    intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
                    intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
                    intent.action == "com.htc.intent.action.QUICKBOOT_POWERON")
        ) {
            return
        }
        logger.v(TAG, "Restoring alarm and scheduling download after action: ${intent.action}")
        val pendingResult = goAsync()
        coroutineScope.launch {
            try {
                alarmHandler.onAlarmFired()
            } catch (e: Exception) {
                logger.v(TAG, "Error restoring alarm on reboot: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
        // App restarted or updated. ForceRemindPastDue so that notifications that were dismissed
        // from restart or update can be shown
        contentDownloadRequestHandler.requestDelayedDownload(forceRemindPastDue = true)
    }

    companion object {
        private const val TAG = "RestartBroadcastReceiver"
    }
}
