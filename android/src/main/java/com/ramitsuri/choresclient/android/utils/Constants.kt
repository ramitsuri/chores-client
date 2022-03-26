package com.ramitsuri.choresclient.android.utils

import androidx.annotation.StringRes
import com.ramitsuri.choresclient.android.R
import java.time.Duration

object Base {
    const val API_BASE_URL = "https://chores-326817.ue.r.appspot.com"
    const val API_TIME_OUT = 60_000
    val SNOOZE_HOUR = Duration.ofHours(6).seconds
    val SNOOZE_DAY = Duration.ofDays(1).seconds
}

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
    );
}

object NotificationActionExtra {
    const val KEY_ASSIGNMENT_ID = "NOTIFICATION_ACTION_KEY_ASSIGNMENT_ID"
    const val KEY_NOTIFICATION_ID = "NOTIFICATION_ACTION_KEY_NOTIFICATION_ID"
    const val KEY_NOTIFICATION_TEXT = "NOTIFICATION_ACTION_KEY_NOTIFICATION_TEXT"
}