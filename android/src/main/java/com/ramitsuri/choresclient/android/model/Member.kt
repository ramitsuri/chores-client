package com.ramitsuri.choresclient.android.model

import com.ramitsuri.choresclient.android.data.MemberEntity
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Member(
    val id: String,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant
) {
    constructor(memberEntity: MemberEntity): this(
        memberEntity.id,
        memberEntity.name,
        memberEntity.createdDate
    )
}