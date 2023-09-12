package com.ramitsuri.choresclient.model.entities

import com.ramitsuri.choresclient.db.HouseEntity
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import kotlinx.datetime.Instant

data class House(
    val id: String,
    val name: String,
    val createdByMemberId: String,
    val createdDate: Instant,
    val status: ActiveStatus
)

fun HouseEntity.toHouse(): House {
    return House(
        id = id,
        name = name,
        createdByMemberId = createdByMemberId,
        createdDate = createdDate,
        status = status
    )
}