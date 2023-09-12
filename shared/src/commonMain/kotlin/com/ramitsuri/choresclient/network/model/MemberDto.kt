package com.ramitsuri.choresclient.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MemberDto(
    val id: String,
    val name: String
)