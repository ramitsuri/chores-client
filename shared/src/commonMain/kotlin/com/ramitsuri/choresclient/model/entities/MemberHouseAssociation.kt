package com.ramitsuri.choresclient.model.entities

import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.MemberHouseEntity

data class MemberHouseAssociation(
    val member: Member,
    val houseId: String
)

fun MemberHouseEntity.toMemberHouseAssociation(memberEntity: MemberEntity): MemberHouseAssociation {
    return MemberHouseAssociation(
        member = Member(
            id = memberEntity.id,
            name = memberEntity.name
        ),
        houseId = houseId
    )
}