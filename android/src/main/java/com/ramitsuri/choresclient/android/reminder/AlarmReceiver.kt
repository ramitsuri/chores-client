package com.ramitsuri.choresclient.android.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ramitsuri.choresclient.reminder.AlarmHandler
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmReceiver : BroadcastReceiver(), KoinComponent {
    private val coroutineScope: CoroutineScope by inject()
    private val alarmHandler: AlarmHandler by inject()
    private val logger: LogHelper by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SHOW_NOTIFICATION) {
            return
        }
        logger.v(TAG, "Alarm triggered: ${intent.action}")
        val pendingResult = goAsync()
        coroutineScope.launch {
            try {
                alarmHandler.onAlarmFired()
            } catch (e: Exception) {
                logger.v(TAG, "Error handling alarm: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.ramitsuri.choresclient.ACTION_SHOW_NOTIFICATION"
        private const val TAG = "AlarmReceiver"
    }
}
