package com.ramitsuri.choresclient.network.model

import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.model.enums.CreateType
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.network.CreateTypeSerializer
import com.ramitsuri.choresclient.network.InstantSerializer
import com.ramitsuri.choresclient.network.LocalDateTimeSerializer
import com.ramitsuri.choresclient.network.ProgressStatusSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class TaskAssignmentDto(
    val id: String,
    @Serializable(with = ProgressStatusSerializer::class)
    val progressStatus: ProgressStatus,
    @Serializable(with = InstantSerializer::class)
    val progressStatusDate: Instant,
    val task: TaskDto,
    val member: MemberDto,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dueDateTime: LocalDateTime,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant,
    @Serializable(with = CreateTypeSerializer::class)
    val createType: CreateType
)

fun TaskAssignmentDto.toTaskAssignmentEntity(shouldUpload: Boolean): TaskAssignmentEntity {
    return TaskAssignmentEntity(
        id = id,
        progressStatusDate = progressStatusDate,
        progressStatus = progressStatus,
        taskId = task.id,
        memberId = member.id,
        dueDateTime = dueDateTime,
        createDate = createdDate,
        createType = createType,
        shouldUpload = shouldUpload
    )
}