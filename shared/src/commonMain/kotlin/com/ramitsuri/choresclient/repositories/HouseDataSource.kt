package com.ramitsuri.choresclient.repositories

import com.ramitsuri.choresclient.data.House
import com.ramitsuri.choresclient.data.entities.HouseDao
import com.ramitsuri.choresclient.db.HouseEntity

class HouseDataSource(
    private val houseDao: HouseDao
) {

    suspend fun saveHouses(houses: List<House>) {
        houseDao.clearAndInsert(houses.map { toHouseEntity(it) })
    }

    suspend fun get(id: String): House? {
        val houseEntity = houseDao.get(id) ?: return null
        return toHouse(houseEntity)
    }

    suspend fun get(): List<House> {
        return houseDao.get().map { toHouse(it) }
    }

    private fun toHouse(houseEntity: HouseEntity): House {
        return House(
            id = houseEntity.id,
            name = houseEntity.name,
            createdByMemberId = houseEntity.createdByMemberId,
            createdDate = houseEntity.createdDate,
            status = houseEntity.status
        )
    }

    private fun toHouseEntity(house: House): HouseEntity {
        return HouseEntity(
            id = house.id,
            name = house.name,
            createdByMemberId = house.createdByMemberId,
            createdDate = house.createdDate,
            status = house.status
        )
    }
}