package com.ramitsuri.choresclient.network.model

import com.ramitsuri.choresclient.db.HouseEntity
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.network.ActiveStatusSerializer
import com.ramitsuri.choresclient.network.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HouseDto(
    val id: String,
    val name: String,
    val createdByMemberId: String,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant,
    @Serializable(with = ActiveStatusSerializer::class)
    val status: ActiveStatus
)

fun HouseDto.toHouseEntity(): HouseEntity {
    return HouseEntity(
        id = id,
        name = name,
        createdByMemberId = createdByMemberId,
        createdDate = createdDate,
        status = status
    )
}