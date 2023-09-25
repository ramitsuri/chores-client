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
        // If system notification id is being sent as -1, intention is to auto generate it.
        // Otherwise, a row for assignment id already exists and we want to replace just the
        // showAtTime and keep the same system notification id
        if (alarm.systemNotificationId == -1L) {
            dbQueries.insertAlarm(
                alarm.assignmentId,
                alarm.showAtTime,
            )
        } else {
            dbQueries.insertAlarmWithReplace(
                alarm.systemNotificationId,
                alarm.assignmentId,
                alarm.showAtTime,
            )
        }
    }
}
