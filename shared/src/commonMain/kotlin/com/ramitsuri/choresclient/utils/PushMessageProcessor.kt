package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.push.PushMessageAction
import com.ramitsuri.choresclient.model.push.PushMessagePayload
import com.ramitsuri.choresclient.notification.CompletedByOthersNotificationHandler

class PushMessageProcessor(
    private val completedByOthersNotificationHandler: CompletedByOthersNotificationHandler,
    private val contentDownloadRequestHandler: ContentDownloadRequestHandler,
    private val appLifecycleObserver: AppLifecycleObserver,
) {
    fun onPushMessageReceived(data: Map<String, String>) {
        val pushMessagePayload = PushMessagePayload.fromMap(data) ?: return

        performAction(pushMessagePayload.action)
        generateNotification(
            doneByOthers = pushMessagePayload.doneByOthers,
            wontDoByOthers = pushMessagePayload.wontDoByOthers
        )
    }

    private fun performAction(action: PushMessageAction) {
        when (action) {
            PushMessageAction.REFRESH_TASK_ASSIGNMENTS -> {
                contentDownloadRequestHandler.requestImmediateDownload()
            }
        }
    }

    private fun generateNotification(doneByOthers: List<String>, wontDoByOthers: List<String>) {
        if (doneByOthers.isEmpty().and(wontDoByOthers.isEmpty())) {
            return
        }
        completedByOthersNotificationHandler.showNotification(
            tasksDoneByOthersNames = doneByOthers, tasksWontDoByOthersName = wontDoByOthers
        )
    }
}