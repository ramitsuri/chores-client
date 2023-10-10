package com.ramitsuri.choresclient.android.notification

import android.content.Context
import com.ramitsuri.choresclient.notification.CompletedByOthersNotificationHandler
import com.ramitsuri.choresclient.notification.NotificationInfo
import com.ramitsuri.choresclient.notification.NotificationManager
import com.ramitsuri.choresclient.notification.Priority
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.utils.NotificationId

class AndroidCompletedByOthersNotificationHandler(
    private val context: Context,
    private val notificationManager: NotificationManager
) : CompletedByOthersNotificationHandler {
    override fun showNotification(
        tasksDoneByOthersNames: List<String>,
        tasksWontDoByOthersName: List<String>
    ) {

        val additionalText = buildString {
            append(context.getString(R.string.notification_by_others_body))
            append("\n")
            if (tasksDoneByOthersNames.isNotEmpty()) {
                append(context.getString(R.string.notification_by_others_done))
                append("\n")
                tasksDoneByOthersNames.forEachIndexed { index, name ->
                    append("- $name")
                    if (index != tasksDoneByOthersNames.lastIndex) {
                        append("\n")
                    }
                }
                append("\n")
            }
            if (tasksWontDoByOthersName.isNotEmpty()) {
                append(context.getString(R.string.notification_by_others_wont_do))
                append("\n")
                tasksWontDoByOthersName.forEachIndexed { index, name ->
                    append("- $name")
                    if (index != tasksWontDoByOthersName.lastIndex) {
                        append("\n")
                    }
                }
            }
        }

        val notificationInfo = NotificationInfo(
            id = NotificationId.COMPLETED_BY_OTHERS,
            channelId = context.getString(R.string.notification_by_others_id),
            priority = Priority.HIGH,
            title = context.getString(R.string.notification_by_others_title),
            body = null,
            additionalText = additionalText,
            iconResId = R.drawable.ic_notification,
        )
        notificationManager.showNotification(notificationInfo)
    }
}