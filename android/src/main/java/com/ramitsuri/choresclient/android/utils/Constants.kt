package com.ramitsuri.choresclient.android.utils

import androidx.annotation.StringRes
import com.ramitsuri.choresclient.android.R

enum class NotificationAction(val action: String, val requestCode: Int, @StringRes val text: Int) {
    SNOOZE_HOUR(
        action = "SNOOZE_HOUR",
        requestCode = 1,
        text = R.string.notification_reminder_action_snooze_hours
    ),
    SNOOZE_DAY(
        action = "SNOOZE_DAY",
        requestCode = 2,
        text = R.string.notification_reminder_action_snooze_day
    ),
    COMPLETE(
        action = "COMPLETE",
        requestCode = 3,
        text = R.string.notification_reminder_action_complete
    ),
    WONT_DO(
        action = "WONT_DO",
        requestCode = 4,
        text = R.string.notification_reminder_action_wont_do
    );

    companion object {
        fun fromAction(action: String): NotificationAction {
            return when (action) {
                SNOOZE_DAY.action -> {
                    SNOOZE_DAY
                }

                WONT_DO.action -> {
                    WONT_DO
                }

                COMPLETE.action -> {
                    COMPLETE
                }

                else -> {
                    SNOOZE_HOUR
                }
            }
        }
    }
}

object NotificationActionExtra {
    const val KEY_ASSIGNMENT_ID = "NOTIFICATION_ACTION_KEY_ASSIGNMENT_ID"
}

object NotificationId {
    const val COMPLETED_BY_OTHERS = Int.MAX_VALUE
    const val CONTENT_DOWNLOAD_FOREGROUND_WORKER = Int.MAX_VALUE - 1
}