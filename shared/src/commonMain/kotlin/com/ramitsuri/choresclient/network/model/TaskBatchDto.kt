package com.ramitsuri.choresclient.network.model

import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.network.ActiveStatusSerializer
import com.ramitsuri.choresclient.network.LocalDateTimeSerializer
import com.ramitsuri.choresclient.network.RepeatUnitSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskBatchResponseDto(
    val successfulRequestIds: List<String> = emptyList(),
    val deleteTaskAssignmentsTaskIds: List<String> = emptyList()
)

@Serializable
sealed class TaskBatchDto {
    abstract val clientRequestId: String

    @Serializable
    @SerialName("add")
    data class Add(
        val name: String,
        val description: String,
        @Serializable(with = LocalDateTimeSerializer::class)
        val dueDateTime: LocalDateTime,
        val houseId: String,
        val memberId: String,
        override val clientRequestId: String,
        val repeatValue: Int? = 0,
        @Serializable(with = RepeatUnitSerializer::class)
        val repeatUnit: RepeatUnit? = RepeatUnit.NONE,
        @Serializable(with = LocalDateTimeSerializer::class)
        val repeatEndDateTime: LocalDateTime? = null,
        val rotateMember: Boolean? = false,
        @Serializable(with = ActiveStatusSerializer::class)
        val status: ActiveStatus? = ActiveStatus.ACTIVE,
    ) : TaskBatchDto()

    @Serializable
    @SerialName("edit")
    data class Edit(
        val taskId: String,
        override val clientRequestId: String,
        val name: String? = null,
        val description: String? = null,
        @Serializable(with = LocalDateTimeSerializer::class)
        val dueDateTime: LocalDateTime? = null,
        val repeatValue: Int? = null,
        @Serializable(with = RepeatUnitSerializer::class)
        val repeatUnit: RepeatUnit? = null,
        @Serializable(with = LocalDateTimeSerializer::class)
        val repeatEndDateTime: LocalDateTime? = null,
        val rotateMember: Boolean? = null,
        @Serializable(with = ActiveStatusSerializer::class)
        val status: ActiveStatus? = null,
    ) : TaskBatchDto()
}
