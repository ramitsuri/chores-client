package com.ramitsuri.choresclient.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.model.entities.TaskAssignmentUpdate
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TaskAssignmentDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    fun getAllFlow(): Flow<List<TaskAssignmentEntity>> {
        return dbQueries
            .selectAssignments()
            .asFlow()
            .mapToList(dispatcherProvider.io)
    }

    suspend fun get(id: String): TaskAssignmentEntity? {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAssignment(id).executeAsOneOrNull()
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
