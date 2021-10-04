package com.ramitsuri.choresclient.android.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Member(
    val id: String,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant
)