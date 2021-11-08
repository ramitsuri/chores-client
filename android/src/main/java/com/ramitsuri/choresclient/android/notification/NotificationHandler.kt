package com.ramitsuri.choresclient.android.notification

interface NotificationHandler {
    fun createChannels(channels: List<NotificationChannelInfo>)

    fun buildAndShow(notificationInfo: NotificationInfo)

    fun cancelNotification(notificationId: Int)
}