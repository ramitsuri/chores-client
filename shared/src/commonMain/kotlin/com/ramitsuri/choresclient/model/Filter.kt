package com.ramitsuri.choresclient.model

data class PersonFilter(
    private val text: TextValue,
    private val items: List<PersonFilterItem>
) : Filter {
    override fun getType() = FilterType.PERSON
    override fun getDisplayText() = text
    override fun getItems() = items
}

data class PersonFilterItem(
    private val id: String,
    private val displayName: String,
    private val selected: Boolean
) : FilterItem {
    override fun getId() = id
    override fun getDisplayName() = displayName
    override fun getIsSelected() = selected
}

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
    fun getDisplayName(): String
    fun getIsSelected(): Boolean
}

enum class FilterType {
    PERSON
}