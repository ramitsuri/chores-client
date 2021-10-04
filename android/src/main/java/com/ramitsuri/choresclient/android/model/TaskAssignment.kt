package com.ramitsuri.choresclient.android.model

import kotlinx.serialization.Serializable
import java.time.Instant

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
)

@Serializable
data class TaskAssignmentDto(
    val progressStatus: Int
)


