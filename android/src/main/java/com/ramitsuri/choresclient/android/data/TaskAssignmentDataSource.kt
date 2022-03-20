package com.ramitsuri.choresclient.android.data

import com.ramitsuri.choresclient.android.model.Member
import com.ramitsuri.choresclient.android.model.Task
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.ui.assigments.FilterMode
import java.time.Instant
import javax.inject.Inject

class TaskAssignmentDataSource @Inject constructor(
    private val taskAssignmentDao: TaskAssignmentDao,
    private val memberDao: MemberDao,
    private val taskDao: TaskDao
) {
    suspend fun saveTaskAssignments(assignments: List<TaskAssignment>) {
        val members = assignments.map { MemberEntity(it.member) }
        memberDao.clearAndInsert(members)

        val tasks = assignments.map { TaskEntity(it.task) }
        taskDao.clearAndInsert(tasks)

        val taskAssignments = assignments.map { TaskAssignmentEntity(it) }
        // Do not do clearAndInsert as there might be local assignments that have been completed
        // but not uploaded
        taskAssignmentDao.clearAndInsert(taskAssignments)
    }

    suspend fun update(assignment: TaskAssignment, readyForUpload: Boolean): Int {
        val taskAssignment = TaskAssignmentEntity(assignment).copy(shouldUpload = readyForUpload)
        return taskAssignmentDao.update(taskAssignment)
    }

    suspend fun getTaskAssignments(filterMode: FilterMode = FilterMode.ALL): List<TaskAssignment> {
        return toTaskAssignments(taskAssignmentDao.get(filterMode))
    }

    suspend fun getTaskAssignment(id: String): TaskAssignment? {
        return toTaskAssignment(taskAssignmentDao.get(id))
    }

    /**
     * Will always return locally saved assignments since the passed due date time
     */
    suspend fun getSince(dueDateTime: Instant): List<TaskAssignment> {
        return toTaskAssignments(taskAssignmentDao.getSince(dueDateTime.toEpochMilli()))
    }

    suspend fun getReadyForUpload(): List<TaskAssignment> {
        return toTaskAssignments(taskAssignmentDao.getForUpload())
    }

    suspend fun delete(taskAssignmentIds: List<String>) {
        taskAssignmentDao.delete(taskAssignmentIds)
    }

    private suspend fun toTaskAssignments(
        taskAssignmentEntities: List<TaskAssignmentEntity>
    ): List<TaskAssignment> {
        val assignments = mutableListOf<TaskAssignment>()
        taskAssignmentEntities.forEach { assignmentEntity ->
            val assignment = toTaskAssignment(assignmentEntity)
            if (assignment != null) {
                assignments.add(assignment)
            }
        }
        return assignments
    }

    private suspend fun toTaskAssignment(assignmentEntity: TaskAssignmentEntity?): TaskAssignment? {
        if (assignmentEntity == null) {
            return null
        }
        val memberEntity = memberDao.get(assignmentEntity.memberId)
        val taskEntity = taskDao.get(assignmentEntity.taskId)
        if (memberEntity == null || taskEntity == null) {
            return null
        }

        return TaskAssignment(
            assignmentEntity,
            Member(memberEntity),
            Task(taskEntity)
        )
    }
}