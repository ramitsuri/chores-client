package com.ramitsuri.choresclient.data

sealed class FilterMode {
    object NONE: FilterMode()
    object ALL: FilterMode()
    data class MINE(val memberId: String): FilterMode()
    data class OTHER(val ownUserId: String): FilterMode()
}