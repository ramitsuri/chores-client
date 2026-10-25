package com.ramitsuri.choresclient.model.entities

import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import kotlin.time.Instant
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
    val status: ActiveStatus,
) {
    constructor(taskEntity: TaskEntity) : this(
        id = taskEntity.id,
        name = taskEntity.name,
        description = taskEntity.description,
        dueDateTime = taskEntity.dueDateTime,
        repeatValue = taskEntity.repeatValue.toInt(),
        repeatUnit = taskEntity.repeatUnit,
        repeatEndDateTime = taskEntity.repeatEndDateTime,
        houseId = taskEntity.houseId,
        memberId = taskEntity.memberId,
        rotateMember = taskEntity.rotateMember,
        createdDate = taskEntity.createdDate,
        status = taskEntity.status,
    )
}
