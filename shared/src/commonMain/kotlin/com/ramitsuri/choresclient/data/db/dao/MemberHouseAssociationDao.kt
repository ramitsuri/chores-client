package com.ramitsuri.choresclient.data.db.dao

import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.db.MemberHouseEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.withContext

class MemberHouseAssociationDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun getForHouse(houseId: String): List<MemberHouseEntity> {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectMemberHouseAssociationsForHouse(houseId)
                .executeAsList()
        }
    }

    suspend fun clearAndInsert(memberHouseEntities: List<MemberHouseEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                deleteAll()
                memberHouseEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    private fun deleteAll() {
        dbQueries.deleteAllMemberHouseAssociations()
    }

    private fun insert(memberHouseEntity: MemberHouseEntity) {
        dbQueries.insertMemberHouseAssociation(
            id = memberHouseEntity.id,
            houseId = memberHouseEntity.houseId,
            memberId = memberHouseEntity.memberId
        )
    }
}