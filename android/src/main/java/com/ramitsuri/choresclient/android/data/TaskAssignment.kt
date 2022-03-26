package com.ramitsuri.choresclient.android.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ramitsuri.choresclient.android.model.CreateType
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.ui.assigments.FilterMode
import java.time.Instant

@Entity(tableName = "TaskAssignments")
data class TaskAssignmentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "progressStatus")
    val progressStatus: ProgressStatus,
    @ColumnInfo(name = "progressStatusDate")
    val progressStatusDate: Instant,
    @ColumnInfo(name = "taskId")
    val taskId: String,
    @ColumnInfo(name = "memberId")
    val memberId: String,
    @ColumnInfo(name = "dueDateTime")
    val dueDateTime: Instant,
    @ColumnInfo(name = "createDate")
    val createdDate: Instant,
    @ColumnInfo(name = "createType")
    val createType: CreateType,
    @ColumnInfo(name = "shouldUpload")
    val shouldUpload: Boolean
) {
    constructor(taskAssignment: TaskAssignment) : this(
        taskAssignment.id,
        taskAssignment.progressStatus,
        taskAssignment.progressStatusDate,
        taskAssignment.task.id,
        taskAssignment.member.id,
        taskAssignment.dueDateTime,
        taskAssignment.createdDate,
        taskAssignment.createType,
        shouldUpload = false
    )
}

@Dao
abstract class TaskAssignmentDao {
    @Query("SELECT * FROM TaskAssignments")
    abstract suspend fun getAll(): List<TaskAssignmentEntity>

    @Query("SELECT * FROM TaskAssignments WHERE progressStatus = 1")
    abstract suspend fun getTodo(): List<TaskAssignmentEntity>

    @Query("SELECT * FROM TaskAssignments WHERE id = :id")
    abstract suspend fun get(id: String): TaskAssignmentEntity?

    @Query("SELECT * FROM TaskAssignments WHERE memberId = :memberId")
    abstract suspend fun getForMember(memberId: String): List<TaskAssignmentEntity>

    @Query("SELECT * FROM TaskAssignments WHERE dueDateTime >= :time")
    abstract suspend fun getSince(time: Long): List<TaskAssignmentEntity>

    @Query("SELECT * FROM TaskAssignments WHERE memberId != :memberId")
    abstract suspend fun getForExceptMember(memberId: String): List<TaskAssignmentEntity>

    @Query("SELECT * FROM TaskAssignments WHERE shouldUpload = 1")
    abstract suspend fun getForUpload(): List<TaskAssignmentEntity>

    @Update(entity = TaskAssignmentEntity::class)
    abstract suspend fun update(taskAssignmentUpdate: TaskAssignmentUpdate): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(taskAssignmentEntities: List<TaskAssignmentEntity>)

    @Transaction
    @Query("DELETE FROM TaskAssignments WHERE id IN (:ids)")
    abstract suspend fun delete(ids: List<String>)

    @Transaction
    open suspend fun get(filterMode: FilterMode): List<TaskAssignmentEntity> {
        return when (filterMode) {
            is FilterMode.NONE -> {
                listOf()
            }
            is FilterMode.ALL -> {
                getAll()
            }
            is FilterMode.MINE -> {
                getForMember(filterMode.memberId)
            }
            is FilterMode.OTHER -> {
                getForExceptMember(filterMode.ownUserId)
            }
        }
    }

    @Transaction
    open suspend fun clearAndInsert(taskAssignmentEntities: List<TaskAssignmentEntity>) {
        // Delete only incomplete assignments and not completed ones that haven't been uploaded.
        // Even though it's unlikely to happen that a completed assignment is not uploaded at this
        // point in the real flow
        val todo = getTodo()
        delete(todo.map { it.id })
        insert(taskAssignmentEntities)
    }
}

data class TaskAssignmentUpdate(
    val id: String,
    val progressStatus: ProgressStatus,
    val progressStatusDate: Instant,
    val shouldUpload: Boolean
)