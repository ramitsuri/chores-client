package com.ramitsuri.choresclient.android.model

import android.os.Parcelable
import com.ramitsuri.choresclient.android.data.TaskEntity
import java.time.Instant
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
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
) : Parcelable {
    constructor(taskEntity: TaskEntity) : this(
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