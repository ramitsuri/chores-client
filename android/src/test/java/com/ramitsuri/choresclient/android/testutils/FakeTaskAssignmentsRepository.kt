package com.ramitsuri.choresclient.android.testutils

import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.ui.assigments.FilterMode
import java.time.Instant

class FakeTaskAssignmentsRepository: TaskAssignmentsRepository {
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

    override suspend fun getLocal(filterMode: FilterMode): Result<List<TaskAssignment>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateTaskAssignment(
        taskAssignment: TaskAssignment,
        readyForUpload: Boolean
    ) {
        TODO("Not yet implemented")
    }
}