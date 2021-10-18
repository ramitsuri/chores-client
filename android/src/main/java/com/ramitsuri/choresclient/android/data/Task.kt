package com.ramitsuri.choresclient.android.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.Task
import java.time.Instant

@Entity(tableName = "Tasks")
class TaskEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "dueDateTime")
    val dueDateTime: Instant,
    @ColumnInfo(name = "repeatValue")
    val repeatValue: Int,
    @ColumnInfo(name = "repeatUnit")
    val repeatUnit: RepeatUnit,
    @ColumnInfo(name = "houseId")
    val houseId: String,
    @ColumnInfo(name = "memberId")
    val memberId: String,
    @ColumnInfo(name = "rotateMember")
    val rotateMember: Boolean,
    @ColumnInfo(name = "createdDate")
    val createdDate: Instant
) {
    constructor(task: Task): this(
        task.id,
        task.name,
        task.description,
        task.dueDateTime,
        task.repeatValue,
        task.repeatUnit,
        task.houseId,
        task.memberId,
        task.rotateMember,
        task.createdDate
    )
}

@Dao
abstract class TaskDao {
    @Query("SELECT * FROM Tasks WHERE id = :id")
    abstract suspend fun get(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(taskEntity: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(taskEntities: List<TaskEntity>)

    @Query("DELETE FROM Tasks")
    abstract suspend fun delete()

    @Transaction
    open suspend fun clearAndInsert(taskEntities: List<TaskEntity>) {
        delete()
        insert(taskEntities)
    }
}