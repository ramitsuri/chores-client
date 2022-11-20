package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.House
import com.ramitsuri.choresclient.data.Member
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.resources.LocalizedString

interface SelectionItem {
    fun getId(): String
    fun getDisplayName(): TextValue
    fun getIsSelected(): Boolean

    fun duplicate(selected: Boolean): SelectionItem
}

data class HouseSelectionItem(
    private val house: House,
    private val selected: Boolean
) : SelectionItem {
    override fun getId() = house.id

    override fun getDisplayName() = TextValue.ForString(house.name)

    override fun getIsSelected() = selected

    override fun duplicate(selected: Boolean) = copy(selected = selected)
}

data class MemberSelectionItem(
    private val member: Member,
    private val selected: Boolean
) : SelectionItem {
    override fun getId() = member.id

    override fun getDisplayName() = TextValue.ForString(member.name)

    override fun getIsSelected() = selected

    override fun duplicate(selected: Boolean) = copy(selected = selected)
}

data class RepeatUnitSelectionItem(
    private val repeatUnit: RepeatUnit,
    private val selected: Boolean
) : SelectionItem {
    override fun getId() = repeatUnit.key.toString()

    override fun getDisplayName() =
        when (repeatUnit) {
            RepeatUnit.NONE -> TextValue.ForKey(LocalizedString.REPEAT_UNIT_NONE)
            RepeatUnit.DAY -> TextValue.ForKey(LocalizedString.REPEAT_UNIT_DAY)
            RepeatUnit.WEEK -> TextValue.ForKey(LocalizedString.REPEAT_UNIT_WEEK)
            RepeatUnit.MONTH -> TextValue.ForKey(LocalizedString.REPEAT_UNIT_MONTH)
            RepeatUnit.HOUR -> TextValue.ForKey(LocalizedString.REPEAT_UNIT_HOUR)
            RepeatUnit.YEAR -> TextValue.ForKey(LocalizedString.REPEAT_UNIT_YEAR)
            RepeatUnit.ON_COMPLETE -> TextValue.ForKey(LocalizedString.REPEAT_UNIT_ON_COMPLETE)
        }

    override fun getIsSelected() = selected

    override fun duplicate(selected: Boolean) = copy(selected = selected)
}
