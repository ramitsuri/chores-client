package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem
import com.ramitsuri.choresclient.model.filter.FilterType
import com.ramitsuri.choresclient.model.view.TaskDetails
import com.ramitsuri.choresclient.model.view.TasksViewState
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.FilterHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel(
    private val tasksRepository: TasksRepository,
    private val filterHelper: FilterHelper
) : ViewModel() {

    private var collectJob: Job? = null

    private val _state = MutableStateFlow(TasksViewState())
    val state: StateFlow<TasksViewState> = _state

    private var rawTasks: List<TaskDetails> = emptyList()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(filters = filterHelper.getBaseFilters())
            }
            onFilterUpdated()
        }
        viewModelScope.launch {
            tasksRepository.fetchTasks()
        }
    }

    fun onFilterItemClicked(filter: Filter, clickedFilterItem: FilterItem) {
        val existingFilters = _state.value.filters
        val newClickedFilter = filterHelper.onFilterItemClicked(filter, clickedFilterItem)
        val newFilters = existingFilters
            .filter { it.getType() != filter.getType() }
            .plus(newClickedFilter)
            .sortedBy { it.getType().index }
        _state.update {
            it.copy(filters = newFilters)
        }
        onFilterUpdated()
    }

    private fun onFilterUpdated() {
        collectJob?.cancel()

        collectJob = viewModelScope.launch {
            tasksRepository.getTasksFlow().collect { taskDetailsList ->
                rawTasks = taskDetailsList.sortedBy { it.task.name }
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val filters = _state.value.filters
        val filtered = rawTasks.toMutableList()

        filters.forEach { filter ->
            when (filter.getType()) {
                FilterType.PERSON -> {
                    if (!filter.getItems().any { it.getIsSelected() && it.getId() == Filter.ALL_ID }) {
                        val selectedMemberIds = filter.getItems().filter { it.getIsSelected() }.map { it.getId() }
                        filtered.removeAll { !selectedMemberIds.contains(it.task.memberId) }
                    }
                }

                FilterType.HOUSE -> {
                    if (!filter.getItems().any { it.getIsSelected() && it.getId() == Filter.ALL_ID }) {
                        val selectedHouseIds = filter.getItems().filter { it.getIsSelected() }.map { it.getId() }
                        filtered.removeAll { !selectedHouseIds.contains(it.task.houseId) }
                    }
                }
            }
        }

        _state.update {
            it.copy(
                loading = false,
                tasks = filtered
            )
        }
    }
}
