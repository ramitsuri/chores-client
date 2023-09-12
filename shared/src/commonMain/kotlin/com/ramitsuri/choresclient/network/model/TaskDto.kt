package com.ramitsuri.choresclient.network.model

import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.network.ActiveStatusSerializer
import com.ramitsuri.choresclient.network.InstantSerializer
import com.ramitsuri.choresclient.network.LocalDateTimeSerializer
import com.ramitsuri.choresclient.network.RepeatUnitSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String,
    val name: String,
    val description: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dueDateTime: LocalDateTime,
    val repeatValue: Int,
    @Serializable(with = RepeatUnitSerializer::class)
    val repeatUnit: RepeatUnit,
    @Serializable(with = LocalDateTimeSerializer::class)
    val repeatEndDateTime: LocalDateTime?,
    val houseId: String,
    val memberId: String,
    val rotateMember: Boolean,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant,
    @Serializable(with = ActiveStatusSerializer::class)
    val status: ActiveStatus
) {
    constructor(task: Task) : this(
        id = task.id,
        name = task.name,
        description = task.description,
        dueDateTime = task.dueDateTime,
        repeatValue = task.repeatValue,
        repeatUnit = task.repeatUnit,
        repeatEndDateTime = task.repeatEndDateTime,
        houseId = task.houseId,
        memberId = task.memberId,
        rotateMember = task.rotateMember,
        createdDate = task.createdDate,
        status = task.status

    )
}

fun TaskDto.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        name = name,
        description = description,
        dueDateTime = dueDateTime,
        repeatUnit = repeatUnit,
        repeatValue = repeatValue.toLong(),
        repeatEndDateTime = repeatEndDateTime,
        houseId = houseId,
        memberId = memberId,
        rotateMember = rotateMember,
        createdDate = createdDate,
        status = status
    )
}