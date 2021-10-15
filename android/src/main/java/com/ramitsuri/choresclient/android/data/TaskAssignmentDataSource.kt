package com.ramitsuri.choresclient.android.data

import com.ramitsuri.choresclient.android.model.Member
import com.ramitsuri.choresclient.android.model.Task
import com.ramitsuri.choresclient.android.model.TaskAssignment
import javax.inject.Inject

class TaskAssignmentDataSource @Inject constructor(
    private val taskAssignmentDao: TaskAssignmentDao,
    private val memberDao: MemberDao,
    private val taskDao: TaskDao
) {
    suspend fun saveTaskAssignments(assignments: List<TaskAssignment>) {
        val members = assignments.map {MemberEntity(it.member)}
        memberDao.insert(members)

        val tasks = assignments.map {TaskEntity(it.task)}
        taskDao.insert(tasks)

        val taskAssignments = assignments.map {TaskAssignmentEntity(it)}
        taskAssignmentDao.insert(taskAssignments)
    }

    suspend fun getTaskAssignments(): List<TaskAssignment> {
        val assignments = mutableListOf<TaskAssignment>()
        for (assignmentEntity in taskAssignmentDao.getAll()) {
            val memberEntity = memberDao.get(assignmentEntity.memberId)
            val taskEntity = taskDao.get(assignmentEntity.taskId)
            if (memberEntity == null || taskEntity == null) {
                continue
            }
            assignments.add(
                TaskAssignment(
                    assignmentEntity,
                    Member(memberEntity),
                    Task(taskEntity)
                )
            )
        }
        return assignments
    }
}