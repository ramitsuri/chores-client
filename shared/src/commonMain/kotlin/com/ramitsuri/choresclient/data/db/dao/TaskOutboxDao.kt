package com.ramitsuri.choresclient.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.db.TaskOutboxEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TaskOutboxDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    fun getOutboxFlow(): Flow<List<TaskOutboxEntity>> {
        return dbQueries
            .selectOutboxItems()
            .asFlow()
            .mapToList(dispatcherProvider.io)
    }

    suspend fun getOutboxItems(): List<TaskOutboxEntity> {
        return withContext(dispatcherProvider.io) {
            dbQueries.selectOutboxItems().executeAsList()
        }
    }

    suspend fun insert(outboxEntity: TaskOutboxEntity) {
        withContext(dispatcherProvider.io) {
            dbQueries.insertOutboxItem(
                id = outboxEntity.id,
                type = outboxEntity.type,
                taskId = outboxEntity.taskId,
                name = outboxEntity.name,
                description = outboxEntity.description,
                dueDateTime = outboxEntity.dueDateTime,
                repeatValue = outboxEntity.repeatValue,
                repeatUnit = outboxEntity.repeatUnit,
                repeatEndDateTime = outboxEntity.repeatEndDateTime,
                houseId = outboxEntity.houseId,
                memberId = outboxEntity.memberId,
                rotateMember = outboxEntity.rotateMember,
                status = outboxEntity.status,
                createdDate = outboxEntity.createdDate
            )
        }
    }

    suspend fun delete(id: String) {
        withContext(dispatcherProvider.io) {
            dbQueries.deleteOutboxItem(id)
        }
    }

    suspend fun delete(ids: List<String>) {
        if (ids.isEmpty()) return
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                ids.forEach { id ->
                    dbQueries.deleteOutboxItem(id)
                }
            }
        }
    }
}
