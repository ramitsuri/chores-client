package com.ramitsuri.choresclient.data.entities

import com.ramitsuri.choresclient.db.ChoresDatabaseQueries
import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.withContext

class MemberDao(
    private val dbQueries: ChoresDatabaseQueries,
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun get(id: String): MemberEntity? {
        return withContext(dispatcherProvider.io) {
            return@withContext dbQueries.selectMember(id).executeAsOneOrNull()
        }
    }

    suspend fun insert(memberEntities: List<MemberEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                memberEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    suspend fun delete() {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                dbQueries.deleteMembers()
            }
        }
    }

    suspend fun clearAndInsert(memberEntities: List<MemberEntity>) {
        withContext(dispatcherProvider.io) {
            dbQueries.transaction {
                deleteAll()
                memberEntities.forEach {
                    insert(it)
                }
            }
        }
    }

    private fun deleteAll() {
        dbQueries.deleteMembers()
    }

    private fun insert(memberEntity: MemberEntity) {
        dbQueries.insertMember(
            memberEntity.id,
            memberEntity.name,
            memberEntity.createdDate
        )
    }
}