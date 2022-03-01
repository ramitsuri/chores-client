package com.ramitsuri.choresclient.android.notification

import android.app.NotificationChannel
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes

data class NotificationChannelInfo(
    val id: String,
    val name: String,
    val description: String,
    val importance: Importance
) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toPlatformChannel(): NotificationChannel {
        val importance = importance.toPlatformValue()
        val channel = NotificationChannel(id, name, importance).apply {
            description = description
            vibrationPattern = null
            enableVibration(true)

        }
        return channel
    }
}

data class NotificationInfo(
    val id: Int,
    val channelId: String,
    val priority: Priority,
    @StringRes val titleResId: Int,
    val body: String,
    @DrawableRes val iconResId: Int,
    val actions: List<NotificationActionInfo>? = null
)

data class NotificationActionInfo(
    val action: String,
    @StringRes val textResId: Int,
    val intentReceiverClass: Class<*>,
    val requestCode: Int
)

enum class Importance(private val platformValue: Int) {
    NONE(0),
    MIN(1),
    LOW(2),
    DEFAULT(3),
    HIGH(4),
    MAX(5);

    // Returns the platformValue for now but could change with future Android versions
    fun toPlatformValue(): Int {
        return platformValue
    }
}

enum class Priority(private val platformValue: Int) {
    DEFAULT(0),
    LOW(-1),
    MIN(-2),
    HIGH(1),
    MAX(2);

    // Returns the platformValue for now but could change with future Android versions
    fun toPlatformValue(): Int {
        return platformValue
    }
}