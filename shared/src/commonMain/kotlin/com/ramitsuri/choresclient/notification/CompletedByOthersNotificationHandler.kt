package com.ramitsuri.choresclient.notification

interface CompletedByOthersNotificationHandler {
    fun showNotification(
        tasksDoneByOthersNames: List<String>,
        tasksWontDoByOthersName: List<String>
    )
}