package com.ramitsuri.choresclient.notification

interface NotificationManager {
    fun createChannels(channels: List<NotificationChannelInfo>)

    fun showNotification(notificationInfo: NotificationInfo)

    fun cancelNotification(notificationId: Int)
}