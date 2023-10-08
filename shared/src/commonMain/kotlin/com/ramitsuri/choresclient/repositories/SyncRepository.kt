package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.db.dao.HouseDao
import com.ramitsuri.choresclient.data.db.dao.MemberDao
import com.ramitsuri.choresclient.data.db.dao.MemberHouseAssociationDao
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.House
import com.ramitsuri.choresclient.model.entities.Member
import com.ramitsuri.choresclient.model.entities.MemberHouseAssociation
import com.ramitsuri.choresclient.model.entities.toHouse
import com.ramitsuri.choresclient.model.entities.toMemberHouseAssociation
import com.ramitsuri.choresclient.network.api.SyncApi
import com.ramitsuri.choresclient.network.model.SyncResultDto
import com.ramitsuri.choresclient.network.model.toHouseEntity
import com.ramitsuri.choresclient.network.model.toMemberEntity
import com.ramitsuri.choresclient.network.model.toMemberHouseEntity

@Suppress("MoveVariableDeclarationIntoWhen")
class SyncRepository(
    private val houseDao: HouseDao,
    private val memberHouseAssociationDao: MemberHouseAssociationDao,
    private val memberDao: MemberDao,
    private val syncApi: SyncApi,
) {

    suspend fun refresh(): Result<Unit> {
        val result = syncApi.sync()
        return when (result) {
            is Result.Success -> {
                val syncResult: SyncResultDto = result.data
                houseDao.clearAndInsert(
                    syncResult.associatedLists
                        .map {
                            it.toHouseEntity()
                        }
                )
                memberHouseAssociationDao.clearAndInsert(
                    syncResult.memberListAssociations
                        .map {
                            it.toMemberHouseEntity()
                        }
                )
                memberDao.clearAndInsert(
                    syncResult.memberListAssociations
                        .map {
                            it.toMemberEntity()
                        }
                )
                Result.Success(Unit)
            }

            is Result.Failure -> {
                result
            }
        }
    }

    suspend fun getHouses(): List<House> {
        return houseDao.get().map { it.toHouse() }
    }

    suspend fun getMembers(): List<Member> {
        return memberDao.get().map { Member(it) }
    }

    suspend fun getMemberHouseAssociationsForHouses(houseId: String): List<MemberHouseAssociation> {
        val memberHouseAssociation = memberHouseAssociationDao.getForHouse(houseId)
        val members = memberDao.get()
        return memberHouseAssociation.mapNotNull { memberHouseEntity ->
            val member = members.find { it.id == memberHouseEntity.memberId }
            if (member == null) {
                null
            } else {
                memberHouseEntity.toMemberHouseAssociation(member)
            }
        }
    }

    companion object {
        private const val TAG = "SyncRepository"
    }
}