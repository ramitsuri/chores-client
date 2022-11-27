package com.ramitsuri.choresclient.android.utils

import androidx.annotation.StringRes
import com.ramitsuri.choresclient.android.R

enum class NotificationAction(val action: String, val requestCode: Int, @StringRes val text: Int) {
    SNOOZE_HOUR(
        "SNOOZE_HOUR",
        1,
        R.string.notification_reminder_action_snooze_hours
    ),
    SNOOZE_DAY(
        "SNOOZE_DAY",
        2,
        R.string.notification_reminder_action_snooze_day
    ),
    COMPLETE(
        "COMPLETE",
        3,
        R.string.notification_reminder_action_complete
    ),
    WONT_DO(
        "WONT_DO",
        4,
        R.string.notification_reminder_action_wont_do
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
    const val KEY_NOTIFICATION_ID = "NOTIFICATION_ACTION_KEY_NOTIFICATION_ID"
    const val KEY_NOTIFICATION_TEXT = "NOTIFICATION_ACTION_KEY_NOTIFICATION_TEXT"
}