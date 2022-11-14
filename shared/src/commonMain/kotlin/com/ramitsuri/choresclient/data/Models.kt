package com.ramitsuri.choresclient.data

import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.network.ActiveStatusSerializer
import com.ramitsuri.choresclient.network.CreateTypeSerializer
import com.ramitsuri.choresclient.network.InstantSerializer
import com.ramitsuri.choresclient.network.LocalDateTimeSerializer
import com.ramitsuri.choresclient.network.ProgressStatusSerializer
import com.ramitsuri.choresclient.network.RepeatUnitSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class TaskAssignment(
    val id: String,
    @Serializable(with = ProgressStatusSerializer::class)
    val progressStatus: ProgressStatus,
    @Serializable(with = InstantSerializer::class)
    val progressStatusDate: Instant,
    val task: Task,
    val member: Member,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dueDateTime: LocalDateTime,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant,
    @Serializable(with = CreateTypeSerializer::class)
    val createType: CreateType
) {
    constructor(taskAssignmentEntity: TaskAssignmentEntity, member: Member, task: Task) : this(
        taskAssignmentEntity.id,
        taskAssignmentEntity.progressStatus,
        taskAssignmentEntity.progressStatusDate,
        task,
        member,
        taskAssignmentEntity.dueDateTime,
        taskAssignmentEntity.createDate,
        taskAssignmentEntity.createType
    )
}

@Serializable
data class Task(
    val id: String,
    val name: String,
    val description: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dueDateTime: LocalDateTime,
    val repeatValue: Int,
    @Serializable(with = RepeatUnitSerializer::class)
    val repeatUnit: RepeatUnit,
    val houseId: String,
    val memberId: String,
    val rotateMember: Boolean,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant,
    @Serializable(with = ActiveStatusSerializer::class)
    val status: ActiveStatus
) {
    constructor(taskEntity: TaskEntity) : this(
        taskEntity.id,
        taskEntity.name,
        taskEntity.description,
        taskEntity.dueDateTime,
        taskEntity.repeatValue.toInt(),
        taskEntity.repeatUnit,
        taskEntity.houseId,
        taskEntity.memberId,
        taskEntity.rotateMember,
        taskEntity.createdDate,
        taskEntity.status
    )
}

@Serializable
data class House(
    val id: String,
    val name: String,
    val createdByMemberId: String,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant,
    @Serializable(with = ActiveStatusSerializer::class)
    val status: ActiveStatus
)

@Serializable
data class Member(
    val id: String,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant
) {
    constructor(memberEntity: MemberEntity) : this(
        memberEntity.id,
        memberEntity.name,
        memberEntity.createdDate
    )
}

@Serializable
data class Token(val authToken: String)

@Serializable
data class SyncResult(val associatedLists: List<House>)

enum class ProgressStatus(val key: Int) {
    UNKNOWN(0),
    TODO(1),
    IN_PROGRESS(2),
    DONE(3),
    WONT_DO(4);

    companion object {
        fun fromKey(key: Int): ProgressStatus {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return UNKNOWN
        }
    }
}

enum class CreateType(val key: Int) {
    UNKNOWN(0),
    MANUAL(1),
    AUTO(2);

    companion object {
        fun fromKey(key: Int): CreateType {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return UNKNOWN
        }
    }
}

enum class ActiveStatus(val key: Int) {
    UNKNOWN(0),
    ACTIVE(1),
    INACTIVE(2), // This entity is no longer being used
    PAUSED(3);

    companion object {
        fun fromKey(key: Int): ActiveStatus {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return UNKNOWN
        }
    }
}

enum class ViewError {
    NETWORK,
    LOGIN_REQUEST_FAILED,
    LOGIN_NO_TOKEN,
    TASK_ASSIGNMENT_DETAILS_NULL
}

enum class RepeatUnit(val key: Int) {
    NONE(0),
    DAY(1),
    WEEK(2),
    MONTH(3),
    HOUR(4),
    YEAR(5),
    ON_COMPLETE(6);

    companion object {
        fun fromKey(key: Int): RepeatUnit {
            for (value in values()) {
                if (value.key == key) {
                    return value
                }
            }
            return NONE
        }
    }
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: ViewError) : Result<Nothing>()
}