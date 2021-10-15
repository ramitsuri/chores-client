package com.ramitsuri.choresclient.android.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.ramitsuri.choresclient.android.model.CreateType
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.TaskAssignment
import java.time.Instant

@Entity(tableName = "TaskAssignments")
class TaskAssignmentEntity(
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
    val createType: CreateType
) {
    constructor(taskAssignment: TaskAssignment): this(
        taskAssignment.id,
        taskAssignment.progressStatus,
        taskAssignment.progressStatusDate,
        taskAssignment.task.id,
        taskAssignment.member.id,
        taskAssignment.dueDateTime,
        taskAssignment.createdDate,
        taskAssignment.createType
    )
}

@Dao
interface TaskAssignmentDao {
    @Query("SELECT * FROM TaskAssignments")
    suspend fun getAll(): List<TaskAssignmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskAssignmentEntity: TaskAssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskAssignmentEntities: List<TaskAssignmentEntity>)
}