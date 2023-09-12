package com.ramitsuri.choresclient.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncResultDto(
    val associatedLists: List<HouseDto>,
    val memberListAssociations: List<MemberHouseAssociationDto>
)