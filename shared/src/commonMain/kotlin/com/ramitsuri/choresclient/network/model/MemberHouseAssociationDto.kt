package com.ramitsuri.choresclient.network.model

import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.MemberHouseEntity
import kotlinx.serialization.Serializable

@Serializable
data class MemberHouseAssociationDto(
    val id: String,
    val member: MemberDto,
    val houseId: String
)

fun MemberHouseAssociationDto.toMemberHouseEntity(): MemberHouseEntity {
    return MemberHouseEntity(
        id = id,
        memberId = member.id,
        houseId = houseId,
    )
}

fun MemberHouseAssociationDto.toMemberEntity(): MemberEntity {
    return MemberEntity(
        id = member.id,
        name = member.name,
    )
}