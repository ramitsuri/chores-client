package com.ramitsuri.choresclient.data.db.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AlarmDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun get(id: String): AlarmEntity? {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAlarm(id).executeAsOneOrNull()
        }
    }

    suspend fun get(): List<AlarmEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectAlarms().executeAsList()
        }
    }

    fun getFlow(): Flow<List<AlarmEntity>> {
        return dbQueries
            .selectAlarms()
            .asFlow()
            .mapToList(dispatcherProvider.io)
    }

    suspend fun insert(alarms: List<AlarmEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                alarms.forEach {
                    insert(it)
                }
            }
        }
    }

    suspend fun delete(assignmentIds: List<String>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                assignmentIds.forEach {
                    dbQueries.deleteAlarm(it)
                }
            }
        }
    }

    private fun insert(alarm: AlarmEntity) {
        dbQueries.insertAlarm(
            alarm.assignmentId,
            alarm.showAtTime,
        )
    }
}
