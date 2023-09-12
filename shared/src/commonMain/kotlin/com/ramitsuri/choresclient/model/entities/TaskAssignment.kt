package com.ramitsuri.choresclient.model.entities

import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

data class TaskAssignment(
    val id: String,
    val progressStatus: ProgressStatus,
    val progressStatusDate: Instant,
    val taskId: String,
    val taskName: String,
    val houseId: String,
    val repeatInfo: RepeatInfo,
    val memberId: String,
    val memberName: String,
    val dueDateTime: LocalDateTime,
) {
    companion object {
        fun fromEntities(
            taskAssignmentEntity: TaskAssignmentEntity,
            taskEntity: TaskEntity,
            memberEntity: MemberEntity
        ): TaskAssignment {
            return TaskAssignment(
                id = taskAssignmentEntity.id,
                progressStatus = taskAssignmentEntity.progressStatus,
                progressStatusDate = taskAssignmentEntity.progressStatusDate,
                taskId = taskEntity.id,
                taskName = taskEntity.name,
                houseId = taskEntity.houseId,
                repeatInfo = RepeatInfo(
                    repeatValue = taskEntity.repeatValue.toInt(),
                    repeatUnit = taskEntity.repeatUnit,
                    repeatEndDateTime = taskEntity.repeatEndDateTime
                ),
                memberId = memberEntity.id,
                memberName = memberEntity.name,
                dueDateTime = taskAssignmentEntity.dueDateTime,
            )
        }
    }
}

data class RepeatInfo(
    val repeatValue: Int,
    val repeatUnit: RepeatUnit,
    val repeatEndDateTime: LocalDateTime?
)