package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.data.FilterMode
import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import kotlinx.datetime.Instant

class FakeTaskAssignmentsRepository : TaskAssignmentsRepository {
    override suspend fun refresh(): Result<List<TaskAssignment>> {
        TODO("Not yet implemented")
    }

    private val sinceAssignments = mutableListOf<TaskAssignment>()

    fun setSince(assignments: List<TaskAssignment>) {
        sinceAssignments.clear()
        sinceAssignments.addAll(assignments)
    }

    override suspend fun getLocal(sinceDueDateTime: Instant): List<TaskAssignment> {
        return sinceAssignments
    }

    override suspend fun getLocal(
        filterMode: FilterMode,
        memberId: String
    ): Result<List<TaskAssignment>> {
        TODO("Not yet implemented")
    }

    override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {
        TODO("Not yet implemented")
    }

    override suspend fun getLocal(id: String): TaskAssignment? {
        TODO("Not yet implemented")
    }
}