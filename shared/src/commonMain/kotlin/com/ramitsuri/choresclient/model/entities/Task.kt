package com.ramitsuri.choresclient.model.entities

import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

data class Task(
    val id: String,
    val name: String,
    val description: String,
    val dueDateTime: LocalDateTime,
    val repeatValue: Int,
    val repeatUnit: RepeatUnit,
    val repeatEndDateTime: LocalDateTime?,
    val houseId: String,
    val memberId: String,
    val rotateMember: Boolean,
    val createdDate: Instant,
    val status: ActiveStatus
) {
    constructor(taskEntity: TaskEntity) : this(
        taskEntity.id,
        taskEntity.name,
        taskEntity.description,
        taskEntity.dueDateTime,
        taskEntity.repeatValue.toInt(),
        taskEntity.repeatUnit,
        taskEntity.repeatEndDateTime,
        taskEntity.houseId,
        taskEntity.memberId,
        taskEntity.rotateMember,
        taskEntity.createdDate,
        taskEntity.status
    )
}
