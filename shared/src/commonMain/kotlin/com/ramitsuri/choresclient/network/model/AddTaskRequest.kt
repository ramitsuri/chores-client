package com.ramitsuri.choresclient.network.model

import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.network.ActiveStatusSerializer
import com.ramitsuri.choresclient.network.LocalDateTimeSerializer
import com.ramitsuri.choresclient.network.RepeatUnitSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
class AddTaskRequest(
    val name: String?,
    val description: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dueDateTime: LocalDateTime?,
    val repeatValue: Int?,
    @Serializable(with = RepeatUnitSerializer::class)
    val repeatUnit: RepeatUnit?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val repeatEndDateTime: LocalDateTime?,
    val houseId: String,
    val memberId: String,
    val rotateMember: Boolean?,
    @Serializable(with = ActiveStatusSerializer::class)
    val status: ActiveStatus
)