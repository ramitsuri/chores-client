package com.ramitsuri.choresclient.android.model

import android.os.Parcelable
import com.ramitsuri.choresclient.android.data.MemberEntity
import java.time.Instant
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Member(
    val id: String,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant
) : Parcelable {
    constructor(memberEntity: MemberEntity) : this(
        memberEntity.id,
        memberEntity.name,
        memberEntity.createdDate
    )
}