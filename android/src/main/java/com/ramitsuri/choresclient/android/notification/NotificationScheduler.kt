package com.ramitsuri.choresclient.android.notification

import com.ramitsuri.choresclient.android.keyvaluestore.KeyValueStore
import com.ramitsuri.choresclient.android.model.TaskAssignment

class NotificationScheduler(
    private val notificationHandler: NotificationHandler,
    private val keyValueStore: KeyValueStore
) {
    fun addNewNotifications(taskAssignments: List<TaskAssignment>) {
        for (taskAssignment in taskAssignments) {
            if (!keyValueStore.get(taskAssignment.id, false)) {
                keyValueStore.put(taskAssignment.id, true)
            }
        }
    }
}