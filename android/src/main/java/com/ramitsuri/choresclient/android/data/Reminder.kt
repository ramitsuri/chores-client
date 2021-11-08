package com.ramitsuri.choresclient.android.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction

@Entity(
    tableName = "RequestCodeTimeAssociations",
    indices = [Index(value = ["time"], unique = true)]
)
data class RequestCodeTimeAssociation(
    @PrimaryKey(autoGenerate = true)
    val requestCode: Int = 0,
    @ColumnInfo(name = "time")
    val time: Long
)

@Entity(tableName = "AssignmentTimeAssociations")
data class AssignmentTimeAssociation(
    @PrimaryKey
    val assignmentId: String,
    @ColumnInfo(name = "time")
    val time: Long
)

data class ReminderAssignment(
    val assignmentId: String,
    val time: Long,
    val requestCode: Int
)

data class ReminderAssignmentUpdateResult(
    val reminderAssignment: ReminderAssignment?,
    val oldTimeNoLongerExists: Boolean
)

@Dao
abstract class ReminderAssignmentDao {

    @Query("SELECT * FROM RequestCodeTimeAssociations")
    abstract suspend fun getRequestCodeTimeAssociations(): List<RequestCodeTimeAssociation>

    @Query("SELECT * FROM RequestCodeTimeAssociations where time = :time")
    abstract suspend fun getRequestCodeTimeAssociations(time: Long): List<RequestCodeTimeAssociation>

    @Query("SELECT * FROM RequestCodeTimeAssociations WHERE time = :time")
    abstract suspend fun getRequestCodeTimeAssociation(time: Long): RequestCodeTimeAssociation?

    @Query("SELECT * FROM AssignmentTimeAssociations")
    abstract suspend fun getAssignmentTimeAssociations(): List<AssignmentTimeAssociation>

    @Query("SELECT * FROM AssignmentTimeAssociations where time = :time")
    abstract suspend fun getAssignmentTimeAssociations(time: Long): List<AssignmentTimeAssociation>

    @Query("SELECT * FROM AssignmentTimeAssociations WHERE assignmentId = :taskAssignmentId")
    abstract suspend fun getAssignmentTimeAssociation(taskAssignmentId: String): AssignmentTimeAssociation?

    // Adding duplicate row would change existing row's requestCode with either REPLACE OR IGNORE.
    // We don't want to do that. So if duplicate time is being inserted - abort. This will throw an
    // exception so the caller should check for that or insert only if doesn't exist
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertRequestCodeTimeAssociation(association: RequestCodeTimeAssociation)

    // Adding duplicate row won't have any effect on how the data looks. So, it's fine here
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAssignmentTimeAssociation(association: AssignmentTimeAssociation)

    @Query("DELETE FROM RequestCodeTimeAssociations where time = :time")
    abstract suspend fun deleteRequestCodeTimeAssociation(time: Long)

    @Query("DELETE FROM AssignmentTimeAssociations where assignmentId = :assignmentId")
    abstract suspend fun deleteAssignmentTimeAssociation(assignmentId: String)

    @Transaction
    open suspend fun insert(
        assignmentId: String,
        assignmentTimeMillis: Long
    ): ReminderAssignment? {
        val requestCodeTimeAssociation = RequestCodeTimeAssociation(time = assignmentTimeMillis)
        val assignmentTimeAssociation =
            AssignmentTimeAssociation(assignmentId, assignmentTimeMillis)
        if (getRequestCodeTimeAssociations(assignmentTimeMillis).isEmpty()) {
            insertRequestCodeTimeAssociation(requestCodeTimeAssociation)
        }
        insertAssignmentTimeAssociation(assignmentTimeAssociation)
        return get(assignmentTimeAssociation.assignmentId)
    }

    @Transaction
    open suspend fun get(assignmentId: String): ReminderAssignment? {
        val assignmentTimeAssociation = getAssignmentTimeAssociation(assignmentId)
        if (assignmentTimeAssociation != null) {
            val requestCodeTimeAssociation =
                getRequestCodeTimeAssociation(assignmentTimeAssociation.time)
            if (requestCodeTimeAssociation != null) {
                return ReminderAssignment(
                    assignmentId = assignmentTimeAssociation.assignmentId,
                    time = assignmentTimeAssociation.time,
                    requestCode = requestCodeTimeAssociation.requestCode
                )
            }
        }
        return null
    }

    @Transaction
    open suspend fun updateOrInsert(
        assignmentId: String,
        newTime: Long,
        oldTime: Long
    ): ReminderAssignmentUpdateResult {
        // Old time changes
        deleteAssignmentTimeAssociation(assignmentId)
        val oldTimeNoLongerExists = getAssignmentTimeAssociations(oldTime).isEmpty()
        if (oldTimeNoLongerExists) {
            deleteRequestCodeTimeAssociation(oldTime)
        }

        // New time changes
        insertAssignmentTimeAssociation(
            AssignmentTimeAssociation(
                assignmentId, newTime
            )
        )
        val newTimeReminders = getRequestCodeTimeAssociations(newTime)
        if (newTimeReminders.isEmpty()) {
            insertRequestCodeTimeAssociation(
                RequestCodeTimeAssociation(
                    time = newTime
                )
            )
        }
        val reminderAssignment = get(assignmentId)

        return ReminderAssignmentUpdateResult(reminderAssignment, oldTimeNoLongerExists)
    }
}

