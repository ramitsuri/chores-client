package com.ramitsuri.choresclient.model.push

enum class PushMessageAction(private val value: String) {
    REFRESH_TASK_ASSIGNMENTS("refresh_task_assignments");

    companion object {
        fun fromStringValue(value: String): PushMessageAction? {
            return values().firstOrNull { it.value == value }
        }
    }
}