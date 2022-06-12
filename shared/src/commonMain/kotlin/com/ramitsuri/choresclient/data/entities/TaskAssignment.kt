package com.ramitsuri.choresclient.data.entities

import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class TaskAssignmentDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun getAll(): List<TaskAssignmentEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignments().executeAsList()
        }
    }

    suspend fun getTodo(): List<TaskAssignmentEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectTodoAssignments().executeAsList()
        }
    }

    suspend fun get(id: String): TaskAssignmentEntity? {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignment(id).executeAsOneOrNull()
        }
    }

    suspend fun getForMember(memberId: String): List<TaskAssignmentEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignmentsByMember(memberId).executeAsList()
        }
    }

    suspend fun getSince(time: Long): List<TaskAssignmentEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignmentsSince(Instant.fromEpochMilliseconds(time))
                .executeAsList()
        }
    }

    suspend fun getForExceptMember(memberId: String): List<TaskAssignmentEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignmentsByNotMember(memberId).executeAsList()
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

    suspend fun insert(taskAssignmentEntities: List<TaskAssignmentEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                taskAssignmentEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    suspend fun delete(ids: List<String>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                ids.forEach {
                    delete(it)
                }
            }
        }
    }

    private fun delete(id: String) {
        dbQueries.deleteAssignment(id)
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