package com.ramitsuri.choresclient.model.filter

import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterItem
import com.ramitsuri.choresclient.model.FilterType
import com.ramitsuri.choresclient.model.TextValue

data class HouseFilter(
    private val text: TextValue,
    private val items: List<HouseFilterItem>
) : Filter {
    override fun getType() = FilterType.HOUSE
    override fun getDisplayText() = text
    override fun getItems() = items
}

data class HouseFilterItem(
    private val id: String,
    private val displayName: TextValue,
    private val selected: Boolean
) : FilterItem {
    override fun getId() = id
    override fun getDisplayName() = displayName
    override fun getIsSelected() = selected

    override fun duplicate(selected: Boolean): FilterItem {
        return copy(selected = selected)
    }
}