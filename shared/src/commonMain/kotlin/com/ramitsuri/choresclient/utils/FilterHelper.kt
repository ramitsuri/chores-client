package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem
import com.ramitsuri.choresclient.model.filter.FilterType
import com.ramitsuri.choresclient.model.filter.HouseFilter
import com.ramitsuri.choresclient.model.filter.HouseFilterItem
import com.ramitsuri.choresclient.model.filter.PersonFilter
import com.ramitsuri.choresclient.model.filter.PersonFilterItem
import com.ramitsuri.choresclient.model.view.TextValue
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.resources.LocalizedString

class FilterHelper(
    private val syncRepository: SyncRepository,
    private val prefManager: PrefManager
) {
    suspend fun getBaseFilters(): List<Filter> {
        val houses = syncRepository.getHouses()
        val members = syncRepository.getMembers()
        val filters = mutableListOf<Filter>()

        // Person filter
        val savedPersonFilterIds = prefManager.getSavedPersonFilterIds()
        val personFilterItems = members
            .map { member -> // Create person filter items
                PersonFilterItem(
                    id = member.id,
                    displayName = TextValue.ForString(member.name),
                    selected = savedPersonFilterIds.contains(member.id) ||
                            savedPersonFilterIds.contains(Filter.ALL_ID)
                )
            }
            .plus(
                PersonFilterItem(
                    id = Filter.ALL_ID,
                    displayName = TextValue.ForKey(LocalizedString.FILTER_ALL),
                    selected = savedPersonFilterIds.contains(Filter.ALL_ID)
                )
            )
        if (personFilterItems.count() > 1) {
            val text = getFilterText(personFilterItems, getUnselectedText(FilterType.PERSON))
            filters.add(PersonFilter(text = text, personFilterItems))
        }

        // House filter
        val savedHouseFilterIds = prefManager.getSavedHouseFilterIds()
        val houseFilterItems = houses
            .map { house ->
                HouseFilterItem(
                    id = house.id,
                    displayName = TextValue.ForString(house.name),
                    selected = savedHouseFilterIds.contains(house.id) ||
                            savedHouseFilterIds.contains(Filter.ALL_ID)
                )
            }
            .plus(
                HouseFilterItem(
                    id = Filter.ALL_ID,
                    displayName = TextValue.ForKey(LocalizedString.FILTER_ALL),
                    selected = savedHouseFilterIds.contains(Filter.ALL_ID)
                )
            ).toList()
        if (houseFilterItems.count() > 1) {
            val text = getFilterText(houseFilterItems, getUnselectedText(FilterType.HOUSE))
            filters.add(HouseFilter(text = text, houseFilterItems))
        }

        return filters.sortedBy { it.getType().index }
    }

    fun onFilterItemClicked(filter: Filter, filterItem: FilterItem): Filter {
        val items: List<FilterItem>
        val text: TextValue
        val unselectedText = getUnselectedText(filter.getType())
        if (filterItem.getId() == Filter.ALL_ID) {
            /*
             * ALL Filter Unselected
             */
            if (filterItem.getIsSelected()) {
                items = filter.getItems()
                    .map {
                        it.duplicate(selected = false)
                    }
                text = TextValue.ForKey(unselectedText)
            } else {
                /*
                 * ALL Filter Selected
                 */
                items = filter.getItems()
                    .map {
                        it.duplicate(selected = true)
                    }
                text = getFilterText(items, unselectedText)
            }
        } else {
            val previousSelectionCount = filter.getItems().count { it.getIsSelected() }
            /*
             * Non-ALL Filter Unselected
             */
            if (filterItem.getIsSelected()) {
                // Unselect all filter item as well
                if (previousSelectionCount == filter.getItems().count()) {
                    items = filter.getItems()
                        .map {
                            it.duplicate(
                                selected = if (it.getId() == filterItem.getId() ||
                                    it.getId() == Filter.ALL_ID
                                ) {
                                    false
                                } else {
                                    it.getIsSelected()
                                }
                            )
                        }
                    text = getFilterText(items, unselectedText)
                } else { // Unselect just the filter item
                    items = filter.getItems()
                        .map {
                            it.duplicate(
                                selected = if (it.getId() == filterItem.getId()) {
                                    false
                                } else {
                                    it.getIsSelected()
                                }
                            )
                        }
                    text = getFilterText(items, unselectedText)
                }
            } else {
                /*
                 * Non-ALL Filter Selected
                 */
                val newSelectionCount = previousSelectionCount + 1
                // Select all filter item as well since last unselected item was selected
                if (previousSelectionCount == filter.getItems().count() - 2) {
                    items = filter.getItems()
                        .map {
                            it.duplicate(
                                selected = if (it.getId() == filterItem.getId() ||
                                    it.getId() == Filter.ALL_ID
                                ) {
                                    true
                                } else {
                                    it.getIsSelected()
                                }
                            )
                        }
                    text = getFilterText(items, unselectedText)
                } else { // Select just the filter item
                    items = filter.getItems()
                        .map {
                            it.duplicate(
                                selected = if (it.getId() == filterItem.getId()) {
                                    true
                                } else {
                                    it.getIsSelected()
                                }
                            )
                        }
                    // Text is "selectedItem + other selections count"
                    text = getFilterText(items, unselectedText)
                }
            }
        }
        return when (filter.getType()) {
            FilterType.PERSON -> {
                PersonFilter(text, items.map { it as PersonFilterItem })
            }

            FilterType.HOUSE -> {
                HouseFilter(text, items.map { it as HouseFilterItem })
            }
        }
    }

    private fun getFilterText(items: List<FilterItem>, unselectedText: LocalizedString): TextValue {
        if (items.filter { it.getIsSelected() }.map { it.getId() }.contains(Filter.ALL_ID)) {
            return TextValue.ForKey(LocalizedString.FILTER_ALL)
        }
        return when (val selectedCount = items.count { it.getIsSelected() }) {
            0 -> {
                TextValue.ForKey(unselectedText)
            }

            1 -> {
                items.find { it.getIsSelected() }
                    ?.getDisplayName() ?: TextValue.ForString("")
            }

            else -> {
                val firstSelectionText =
                    items.firstOrNull() { it.getIsSelected() }?.getDisplayName()
                        ?: TextValue.ForString("")
                firstSelectionText.addAdditionalArgs("+${selectedCount - 1}")
            }
        }
    }

    private fun getUnselectedText(filterType: FilterType): LocalizedString {
        return when (filterType) {
            FilterType.PERSON -> {
                LocalizedString.PERSON_FILTER
            }

            FilterType.HOUSE -> {
                LocalizedString.HOUSE_FILTER
            }
        }
    }
}