package com.ramitsuri.choresclient.android.testutils

import android.database.sqlite.SQLiteConstraintException
import com.ramitsuri.choresclient.android.data.AssignmentTimeAssociation
import com.ramitsuri.choresclient.android.data.ReminderAssignment
import com.ramitsuri.choresclient.android.data.ReminderAssignmentDao
import com.ramitsuri.choresclient.android.data.ReminderAssignmentUpdateResult
import com.ramitsuri.choresclient.android.data.RequestCodeTimeAssociation

class FakeReminderAssignmentDao: ReminderAssignmentDao() {
    private var requestCode = 1
    private val requestCodeTimeAssociations = mutableMapOf<Long, Int>() // Time | Request Code
    private val assignmentTimeAssociations = mutableMapOf<String, Long>() // AssignmentId | Time

    private var resultToRespond: ReminderAssignmentUpdateResult? = null

    override suspend fun getRequestCodeTimeAssociations(): List<RequestCodeTimeAssociation> {
        return requestCodeTimeAssociations
            .map {(time, requestCode) ->
                RequestCodeTimeAssociation(
                    requestCode,
                    time
                )
            }
    }

    override suspend fun getRequestCodeTimeAssociations(time: Long): List<RequestCodeTimeAssociation> {
        return requestCodeTimeAssociations
            .filter {(associationTime, requestCode) -> associationTime == time}
            .map {(associationTime, requestCode) ->
                RequestCodeTimeAssociation(
                    requestCode,
                    associationTime
                )
            }
    }

    override suspend fun getRequestCodeTimeAssociation(time: Long): RequestCodeTimeAssociation? {
        val requestCode = requestCodeTimeAssociations[time] ?: return null
        return RequestCodeTimeAssociation(requestCode, time)
    }

    override suspend fun getAssignmentTimeAssociations(): List<AssignmentTimeAssociation> {
        return assignmentTimeAssociations
            .map {(assignmentId, assignmentTime) ->
                AssignmentTimeAssociation(
                    assignmentId,
                    assignmentTime
                )
            }
    }

    override suspend fun getAssignmentTimeAssociations(time: Long): List<AssignmentTimeAssociation> {
        return assignmentTimeAssociations
            .filter {(assignmentId, assignmentTime) -> assignmentTime == time}
            .map {(assignmentId, assignmentTime) ->
                AssignmentTimeAssociation(
                    assignmentId,
                    assignmentTime
                )
            }
    }

    override suspend fun getAssignmentTimeAssociation(taskAssignmentId: String): AssignmentTimeAssociation? {
        val time = assignmentTimeAssociations[taskAssignmentId] ?: return null
        return AssignmentTimeAssociation(taskAssignmentId, time)
    }

    override suspend fun insertRequestCodeTimeAssociation(association: RequestCodeTimeAssociation) {
        if (requestCodeTimeAssociations.keys.contains(association.time)) {
            throw SQLiteConstraintException()
        } else {
            requestCodeTimeAssociations[association.time] = getRequestCode()
        }
    }

    override suspend fun insertAssignmentTimeAssociation(association: AssignmentTimeAssociation) {
        assignmentTimeAssociations[association.assignmentId] = association.time
    }

    override suspend fun deleteRequestCodeTimeAssociation(time: Long) {
        requestCodeTimeAssociations.remove(time)
    }

    override suspend fun deleteAssignmentTimeAssociation(assignmentId: String) {
        assignmentTimeAssociations.remove(assignmentId)
    }

    override suspend fun insert(
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

    override suspend fun get(assignmentId: String): ReminderAssignment? {
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

    override suspend fun updateOrInsert(
        assignmentId: String,
        newTime: Long,
        oldTime: Long
    ): ReminderAssignmentUpdateResult {
        resultToRespond?.let {
            resultToRespond = null
            return it
        }
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

    fun setFakeResult(result: ReminderAssignmentUpdateResult) {
        resultToRespond = result
    }

    private fun getRequestCode(): Int {
        val reqCode = requestCode
        requestCode++
        return reqCode
    }
}