package com.ramitsuri.choresclient.android.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RestartBroadcastReceiver : BroadcastReceiver(), KoinComponent {
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

        // App restarted or updated. ForceRemindPastDue so that notifications that were dismissed
        // from restart or update can be shown
        contentDownloadRequestHandler.requestDelayedDownload(forceRemindPastDue = true)
    }
}