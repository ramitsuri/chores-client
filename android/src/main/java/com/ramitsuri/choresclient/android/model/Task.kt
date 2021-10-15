package com.ramitsuri.choresclient.android.model

import com.ramitsuri.choresclient.android.data.TaskEntity
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Task(
    val id: String,
    val name: String,
    val description: String,
    @Serializable(with = InstantSerializer::class)
    val dueDateTime: Instant,
    val repeatValue: Int,
    @Serializable(with = RepeatUnitSerializer::class)
    val repeatUnit: RepeatUnit,
    val houseId: String,
    val memberId: String,
    val rotateMember: Boolean,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant
) {
    constructor(taskEntity: TaskEntity): this(
        taskEntity.id,
        taskEntity.name,
        taskEntity.description,
        taskEntity.dueDateTime,
        taskEntity.repeatValue,
        taskEntity.repeatUnit,
        taskEntity.houseId,
        taskEntity.memberId,
        taskEntity.rotateMember,
        taskEntity.createdDate
    )
}