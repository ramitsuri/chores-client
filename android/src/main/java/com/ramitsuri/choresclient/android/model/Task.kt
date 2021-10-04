package com.ramitsuri.choresclient.android.model

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
)