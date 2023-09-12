package com.ramitsuri.choresclient.model.entities

import kotlinx.datetime.LocalDateTime

data class AssignmentAlarm(
    val assignmentId: String,
    val showAtTime: LocalDateTime,
    val systemNotificationId: Int = -1,
)