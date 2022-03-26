package com.ramitsuri.choresclient.android.model

import android.os.Parcelable
import com.ramitsuri.choresclient.android.data.TaskAssignmentEntity
import java.time.Instant
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class TaskAssignment(
    val id: String,
    @Serializable(with = ProgressStatusSerializer::class)
    val progressStatus: ProgressStatus,
    @Serializable(with = InstantSerializer::class)
    val progressStatusDate: Instant,
    val task: Task,
    val member: Member,
    @Serializable(with = InstantSerializer::class)
    val dueDateTime: Instant,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant,
    @Serializable(with = CreateTypeSerializer::class)
    val createType: CreateType
) : Parcelable {
    constructor(taskAssignmentEntity: TaskAssignmentEntity, member: Member, task: Task) : this(
        taskAssignmentEntity.id,
        taskAssignmentEntity.progressStatus,
        taskAssignmentEntity.progressStatusDate,
        task,
        member,
        taskAssignmentEntity.dueDateTime,
        taskAssignmentEntity.createdDate,
        taskAssignmentEntity.createType
    )
}


