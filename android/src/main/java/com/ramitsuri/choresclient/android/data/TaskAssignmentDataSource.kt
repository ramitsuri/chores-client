package com.ramitsuri.choresclient.android.data

import com.ramitsuri.choresclient.android.model.Member
import com.ramitsuri.choresclient.android.model.Task
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.ui.assigments.FilterMode
import javax.inject.Inject

class TaskAssignmentDataSource @Inject constructor(
    private val taskAssignmentDao: TaskAssignmentDao,
    private val memberDao: MemberDao,
    private val taskDao: TaskDao
) {
    suspend fun saveTaskAssignments(assignments: List<TaskAssignment>) {
        val members = assignments.map {MemberEntity(it.member)}
        memberDao.clearAndInsert(members)

        val tasks = assignments.map {TaskEntity(it.task)}
        taskDao.clearAndInsert(tasks)

        val taskAssignments = assignments.map {TaskAssignmentEntity(it)}
        taskAssignmentDao.clearAndInsert(taskAssignments)
    }

    suspend fun saveTaskAssignment(assignment: TaskAssignment) {
        val member = MemberEntity(assignment.member)
        memberDao.insert(member)

        val task = TaskEntity(assignment.task)
        taskDao.insert(task)

        val taskAssignment = TaskAssignmentEntity(assignment)
        taskAssignmentDao.insert(taskAssignment)
    }

    suspend fun getTaskAssignments(filterMode: FilterMode = FilterMode.ALL): List<TaskAssignment> {
        val assignments = mutableListOf<TaskAssignment>()
        for (assignmentEntity in taskAssignmentDao.get(filterMode)) {
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