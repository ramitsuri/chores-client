package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.Member
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.Task
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.data.entities.MemberDao
import com.ramitsuri.choresclient.data.entities.TaskAssignmentDao
import com.ramitsuri.choresclient.data.entities.TaskAssignmentUpdate
import com.ramitsuri.choresclient.data.entities.TaskDao
import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterType
import kotlinx.datetime.Instant

class TaskAssignmentDataSource(
    private val taskAssignmentDao: TaskAssignmentDao,
    private val memberDao: MemberDao,
    private val taskDao: TaskDao
) {
    suspend fun saveTaskAssignments(assignments: List<TaskAssignment>) {
        val members = assignments.map {
            MemberEntity(
                it.member.id,
                it.member.name,
                it.member.createdDate
            )
        }
        memberDao.clearAndInsert(members)

        val tasks = assignments.map { taskToTaskEntity(it.task) }
        taskDao.clearAndInsert(tasks)

        val taskAssignments = assignments.map { taskAssignmentToTaskAssignmentEntity(it) }
        // Do not do clearAndInsert as there might be local assignments that have been completed
        // but not uploaded. The insert statement here ignores a row if it already exists.
        taskAssignmentDao.insert(taskAssignments)
    }

    suspend fun markDone(assignmentId: String, doneTime: Instant) {
        val taskAssignment = TaskAssignmentUpdate(
            assignmentId,
            ProgressStatus.DONE,
            doneTime,
            shouldUpload = true
        )
        taskAssignmentDao.update(taskAssignment)
    }

    suspend fun getTaskAssignments(
        filters: List<Filter> = listOf()
    ): List<TaskAssignment> {
        if (filters.isEmpty()) {
            return toTaskAssignments(taskAssignmentDao.getAll())
        }
        val filtered = toTaskAssignments(taskAssignmentDao.getAll()).toMutableList()
        filters.forEach { filter ->
            when (filter.getType()) {
                FilterType.PERSON -> {
                    if (filter.getItems()
                            .any { it.getIsSelected() && it.getId() == Filter.ALL_ID }
                    ) {
                        // All selected, remove nothing
                    } else {
                        val selectedMemberIds =
                            filter.getItems().filter { it.getIsSelected() }.map { it.getId() }
                        filtered.removeAll { !selectedMemberIds.contains(it.member.id) }
                    }
                }
                FilterType.HOUSE -> {
                    if (filter.getItems()
                            .any { it.getIsSelected() && it.getId() == Filter.ALL_ID }
                    ) {
                        // All selected, remove nothing
                    } else {
                        val selectedHouseIds =
                            filter.getItems().filter { it.getIsSelected() }.map { it.getId() }
                        filtered.removeAll { !selectedHouseIds.contains(it.task.houseId) }
                    }
                }
            }
        }
        return filtered
    }

    suspend fun getTaskAssignment(id: String): TaskAssignment? {
        return toTaskAssignment(taskAssignmentDao.get(id))
    }

    suspend fun getTaskAssignmentStatus(id: String): String {
        val assignmentEntity = taskAssignmentDao.get(id)
        return "Upload: ${assignmentEntity?.shouldUpload}, Status: ${assignmentEntity?.progressStatus}"
    }

    /**
     * Will always return locally saved assignments since the passed due date time
     */
    suspend fun getSince(dueDateTime: Instant): List<TaskAssignment> {
        return toTaskAssignments(taskAssignmentDao.getSince(dueDateTime.toEpochMilliseconds()))
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
        val memberEntity = memberDao.get(assignmentEntity.memberId) ?: return null
        val taskEntity = taskDao.get(assignmentEntity.taskId) ?: return null

        return TaskAssignment(
            assignmentEntity,
            Member(memberEntity),
            Task(taskEntity)
        )
    }

    private fun taskToTaskEntity(task: Task): TaskEntity {
        return TaskEntity(
            id = task.id,
            name = task.name,
            description = task.description,
            dueDateTime = task.dueDateTime,
            repeatUnit = task.repeatUnit,
            repeatValue = task.repeatValue.toLong(),
            houseId = task.houseId,
            memberId = task.memberId,
            rotateMember = task.rotateMember,
            createdDate = task.createdDate
        )
    }

    private fun taskAssignmentToTaskAssignmentEntity(taskAssignment: TaskAssignment): TaskAssignmentEntity {
        return TaskAssignmentEntity(
            id = taskAssignment.id,
            progressStatusDate = taskAssignment.progressStatusDate,
            progressStatus = taskAssignment.progressStatus,
            taskId = taskAssignment.task.id,
            memberId = taskAssignment.member.id,
            dueDateTime = taskAssignment.dueDateTime,
            createDate = taskAssignment.createdDate,
            createType = taskAssignment.createType,
            shouldUpload = false // This is used for downloading TaskAssignments from backend.
            // They're not ready for upload yet
        )
    }
}