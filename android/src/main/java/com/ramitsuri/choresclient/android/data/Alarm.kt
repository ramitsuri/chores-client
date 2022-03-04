package com.ramitsuri.choresclient.android.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import java.time.Instant

@Entity(tableName = "Alarms")
class AlarmEntity(
    @PrimaryKey
    val assignmentId: String,
    @ColumnInfo(name = "showAtTime")
    val showAtTime: Instant = Instant.MIN,
    // Id that is used to show the system notification. If -1, notification hasn't been shown
    @ColumnInfo(name = "systemNotificationId")
    val systemNotificationId: Int = -1
)

@Dao
abstract class AlarmDao {
    @Query("SELECT * FROM Alarms")
    abstract suspend fun get(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(alarmEntities: List<AlarmEntity>)

    @Delete
    abstract suspend fun delete(alarmEntities: List<AlarmEntity>)
}

data class AssignmentAlarm(
    val assignmentId: String,
    val showAtTime: Instant = Instant.MIN,
    val systemNotificationId: Int = -1,
    val systemNotificationText: String
)