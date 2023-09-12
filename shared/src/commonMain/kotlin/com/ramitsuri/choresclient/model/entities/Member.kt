package com.ramitsuri.choresclient.model.entities

import com.ramitsuri.choresclient.db.MemberEntity

data class Member(
    val id: String,
    val name: String
) {
    constructor(memberEntity: MemberEntity) : this(
        id = memberEntity.id,
        name = memberEntity.name
    )
}
