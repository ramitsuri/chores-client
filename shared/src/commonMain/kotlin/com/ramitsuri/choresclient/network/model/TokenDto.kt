package com.ramitsuri.choresclient.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenDto(val authToken: String)