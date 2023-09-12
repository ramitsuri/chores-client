package com.ramitsuri.choresclient.model.filter

import com.ramitsuri.choresclient.model.view.TextValue

interface Filter {
    companion object {
        const val ALL_ID = "-1"
    }

    fun getType(): FilterType
    fun getDisplayText(): TextValue
    fun getItems(): List<FilterItem>

    fun getKey(): String = getType().toString().plus(getItems().count { it.getIsSelected() })
}

interface FilterItem {
    fun getId(): String
    fun getDisplayName(): TextValue
    fun getIsSelected(): Boolean

    fun duplicate(selected: Boolean): FilterItem
}

enum class FilterType(val index: Int) {
    PERSON(index = 0),
    HOUSE(index = 1)
}