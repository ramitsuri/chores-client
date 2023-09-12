package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.network.model.TaskAssignmentDto
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

class FakeTaskAssignmentsRepository : TaskAssignmentsRepository {
    override suspend fun refresh(): Result<Unit> {
        TODO("Not yet implemented")
    }

    private val sinceAssignments = mutableListOf<TaskAssignmentDto>()

    fun setSince(assignments: List<TaskAssignmentDto>) {
        sinceAssignments.clear()
        sinceAssignments.addAll(assignments)
    }

    override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {
        TODO("Not yet implemented")
    }

    override suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant) {
        TODO("Not yet implemented")
    }

    override suspend fun getLocal(id: String): TaskAssignment? {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> {
        TODO("Not yet implemented")
    }

    override suspend fun onSnoozeHourRequested(assignmentId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun onSnoozeDayRequested(assignmentId: String) {
        TODO("Not yet implemented")
    }
}