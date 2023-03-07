package com.ramitsuri.choresclient.data.entities

import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

class TaskAssignmentDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun getAll(): List<TaskAssignmentEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignments().executeAsList()
        }
    }

    suspend fun get(id: String): TaskAssignmentEntity? {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignment(id).executeAsOneOrNull()
        }
    }

    suspend fun getSince(time: LocalDateTime): List<TaskAssignmentEntity> {
        return withContext(dispatcherProvider.io) {
            dbQueries.selectAssignments().executeAsList().filter { taskAssignmentEntity ->
                taskAssignmentEntity.dueDateTime > time
            }
        }
    }

    suspend fun getForUpload(): List<TaskAssignmentEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignmentsForUpload().executeAsList()
        }
    }

    suspend fun update(taskAssignmentUpdate: TaskAssignmentUpdate) {
        withContext(dispatcherProvider.io) {
            dbQueries.updateAssignment(
                progressStatus = taskAssignmentUpdate.progressStatus,
                progressStatusDate = taskAssignmentUpdate.progressStatusDate,
                shouldUpload = taskAssignmentUpdate.shouldUpload,
                id = taskAssignmentUpdate.id
            )
        }
    }

    suspend fun clearTodoAndInsert(taskAssignmentEntities: List<TaskAssignmentEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                dbQueries.deleteTodo()
                taskAssignmentEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    private fun insert(taskAssignmentEntity: TaskAssignmentEntity) {
        dbQueries.insertAssignment(
            taskAssignmentEntity.id,
            taskAssignmentEntity.progressStatus,
            taskAssignmentEntity.progressStatusDate,
            taskAssignmentEntity.taskId,
            taskAssignmentEntity.memberId,
            taskAssignmentEntity.dueDateTime,
            taskAssignmentEntity.createDate,
            taskAssignmentEntity.createType,
            taskAssignmentEntity.shouldUpload
        )
    }
}

data class TaskAssignmentUpdate(
    val id: String,
    val progressStatus: ProgressStatus,
    val progressStatusDate: Instant,
    val shouldUpload: Boolean
)