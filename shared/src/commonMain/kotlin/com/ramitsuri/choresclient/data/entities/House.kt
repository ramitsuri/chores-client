package com.ramitsuri.choresclient.data.entities

import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.db.HouseEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.withContext

class HouseDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun get(id: String): HouseEntity? {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectHouse(id).executeAsOneOrNull()
        }
    }

    suspend fun get(): List<HouseEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectHouses().executeAsList()
        }
    }

    suspend fun insert(houseEntities: List<HouseEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                houseEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    suspend fun clearAndInsert(houseEntities: List<HouseEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                deleteAll()
                houseEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    private fun deleteAll() {
        dbQueries.deleteHouses()
    }

    private fun insert(houseEntity: HouseEntity) {
        dbQueries.insertHouse(
            houseEntity.id,
            houseEntity.name,
            houseEntity.createdByMemberId,
            houseEntity.createdDate,
            houseEntity.status
        )
    }
}