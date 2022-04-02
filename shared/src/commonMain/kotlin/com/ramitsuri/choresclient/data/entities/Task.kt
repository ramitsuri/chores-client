package com.ramitsuri.choresclient.data.entities

import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.withContext

class TaskDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun get(id: String): TaskEntity? {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectTask(id).executeAsOneOrNull()
        }
    }

    suspend fun insert(taskEntities: List<TaskEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                taskEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    suspend fun delete() {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                dbQueries.deleteTasks()
            }
        }
    }

    suspend fun clearAndInsert(taskEntities: List<TaskEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                deleteAll()
                taskEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    private fun deleteAll() {
        dbQueries.deleteTasks()
    }

    private fun insert(taskEntity: TaskEntity) {
        dbQueries.insertTask(
            taskEntity.id,
            taskEntity.name,
            taskEntity.description,
            taskEntity.dueDateTime,
            taskEntity.repeatValue,
            taskEntity.repeatUnit,
            taskEntity.houseId,
            taskEntity.memberId,
            taskEntity.rotateMember,
            taskEntity.createdDate
        )
    }

}