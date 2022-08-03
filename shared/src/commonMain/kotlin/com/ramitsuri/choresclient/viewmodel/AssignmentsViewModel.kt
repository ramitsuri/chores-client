package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.AssignmentsViewState
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterItem
import com.ramitsuri.choresclient.model.FilterType
import com.ramitsuri.choresclient.model.PersonFilter
import com.ramitsuri.choresclient.model.PersonFilterItem
import com.ramitsuri.choresclient.model.TaskAssignmentWrapper
import com.ramitsuri.choresclient.model.TextValue
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.resources.LocalizedString
import com.ramitsuri.choresclient.utils.AppHelper
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.getDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AssignmentsViewModel(
    private val assignmentDetailsRepository: AssignmentDetailsRepository,
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager,
    private val appHelper: AppHelper,
    private val dispatchers: DispatcherProvider,
    private val longLivingCoroutineScope: CoroutineScope
) : ViewModel(), KoinComponent {

    private val logger: LogHelper by inject()
    private val userId = prefManager.getUserId() ?: ""
    private var filters = mutableListOf<Filter>()
    private val _state = MutableStateFlow(AssignmentsViewState(filters = filters))
    val state: StateFlow<AssignmentsViewState> = _state

    init {
        fetchAssignments(true)
    }

    fun fetchAssignments(getLocal: Boolean = false) {
        val isWorkerRunning = appHelper.isWorkerRunning()
        val shouldRefresh = !(getLocal || isWorkerRunning)
        logger.d(
            "AssignmentsViewModel",
            "Will refresh: $shouldRefresh - getLocal($getLocal) || workerRunning($isWorkerRunning)"
        )
        _state.update {
            it.copy(loading = true)
        }
        // We want this to be run in long living scope so that the refresh operation isn't cancelled
        // while assignments have been uploaded but not deleted locally for example. Or are being
        // uploaded still
        longLivingCoroutineScope.launch(dispatchers.main) {
            if (shouldRefresh) {
                repository.refresh()
                getLocal(refreshFilters = true)
            } else {
                getLocal(refreshFilters = false)
            }
        }
    }

    fun filter(filter: Filter, filterItem: FilterItem) {
        when (filter.getType()) {
            FilterType.PERSON -> {
                val items: List<FilterItem>
                val text: TextValue
                if (filterItem.getId() == Filter.ALL_ID) { // All filter
                    if (filterItem.getIsSelected()) { // All filter unselected
                        items = filter.getItems()
                            .map {
                                PersonFilterItem(
                                    it.getId(),
                                    it.getDisplayName(),
                                    selected = false
                                )
                            }
                        text = TextValue.ForKey(LocalizedString.PERSON_FILTER)
                    } else { // All filter selected
                        items = filter.getItems()
                            .map {
                                PersonFilterItem(
                                    it.getId(),
                                    it.getDisplayName(),
                                    selected = true
                                )
                            }
                        text = TextValue.ForKey(LocalizedString.FILTER_ALL)
                    }
                } else { // Non All filter
                    val previousSelectionCount = filter.getItems().count { it.getIsSelected() }
                    if (filterItem.getIsSelected()) { // New filter unselected
                        // Unselect all filter item as well
                        if (previousSelectionCount == filter.getItems().count()) {
                            items = filter.getItems()
                                .map {
                                    PersonFilterItem(
                                        it.getId(),
                                        it.getDisplayName(),
                                        selected = if (it.getId() == filterItem.getId() ||
                                            it.getId() == Filter.ALL_ID
                                        ) {
                                            false
                                        } else {
                                            it.getIsSelected()
                                        }
                                    )
                                }
                            val newSelectionCount = items.count { it.getIsSelected() }
                            text = if (newSelectionCount == 0) {
                                TextValue.ForKey(LocalizedString.PERSON_FILTER)
                            } else if (newSelectionCount == 1) {
                                TextValue.ForString(items.find { it.getIsSelected() }
                                    ?.getDisplayName() ?: "")
                            } else {
                                val firstSelectionText =
                                    items.firstOrNull() { it.getIsSelected() }?.getDisplayName()
                                        ?: ""
                                TextValue.ForString("$firstSelectionText+$newSelectionCount")
                            }
                        } else { // Unselect just the filter item
                            items = filter.getItems()
                                .map {
                                    PersonFilterItem(
                                        it.getId(),
                                        it.getDisplayName(),
                                        selected = if (it.getId() == filterItem.getId()) {
                                            false
                                        } else {
                                            it.getIsSelected()
                                        }
                                    )
                                }
                            val newSelectionCount = items.count { it.getIsSelected() }
                            text = if (newSelectionCount == 0) {
                                TextValue.ForKey(LocalizedString.PERSON_FILTER)
                            } else if (newSelectionCount == 1) {
                                TextValue.ForString(items.find { it.getIsSelected() }
                                    ?.getDisplayName() ?: "")
                            } else {
                                val firstSelectionText =
                                    items.firstOrNull() { it.getIsSelected() }?.getDisplayName()
                                        ?: ""
                                TextValue.ForString("$firstSelectionText+$newSelectionCount")
                            }
                        }
                    } else { // New filter selected
                        val newSelectionCount = previousSelectionCount + 1
                        // Select all filter item as well since last unselected item was selected
                        if (previousSelectionCount == filter.getItems().count() - 2) {
                            items = filter.getItems()
                                .map {
                                    PersonFilterItem(
                                        it.getId(),
                                        it.getDisplayName(),
                                        selected = if (it.getId() == filterItem.getId() ||
                                            it.getId() == Filter.ALL_ID
                                        ) {
                                            true
                                        } else {
                                            it.getIsSelected()
                                        }
                                    )
                                }
                            text = TextValue.ForKey(LocalizedString.FILTER_ALL)
                        } else { // Select just the filter item
                            items = filter.getItems()
                                .map {
                                    PersonFilterItem(
                                        it.getId(),
                                        it.getDisplayName(),
                                        selected = if (it.getId() == filterItem.getId()) {
                                            true
                                        } else {
                                            it.getIsSelected()
                                        }
                                    )
                                }
                            // Text is "selectedItem + other selections count"
                            text = if (newSelectionCount == 1) { // Rob
                                TextValue.ForString(filterItem.getDisplayName())
                            } else { // Rob +1
                                TextValue.ForString(
                                    "${filterItem.getDisplayName()}+" +
                                            "${items.count() - 1}"
                                )
                            }
                        }
                    }
                }
                val newFilter = PersonFilter(text, items)
                filters.removeAll { it.getType() == FilterType.PERSON }
                filters.add(newFilter)
                _state.update {
                    it.copy(filters = filters, loading = true)
                }
                getLocal(refreshFilters = false)
            }
        }
    }

    fun changeStateRequested(id: String, progressStatus: ProgressStatus) {
        if (progressStatus != ProgressStatus.TODO) {
            return
        }
        _state.update {
            it.copy(loading = true)
        }
        longLivingCoroutineScope.launch {
            assignmentDetailsRepository.onCompleteRequestedSuspend(id)
            getLocal(refreshFilters = false)
        }
    }

    fun toggleLogging() {
        val currentlyEnabled = prefManager.getEnableRemoteLogging()
        prefManager.setEnableRemoteLogging(!currentlyEnabled)
        logger.enableRemoteLogging(!currentlyEnabled)
    }

    private fun getLocal(refreshFilters: Boolean) {
        viewModelScope.launch(dispatchers.main) {
            val assignmentsResult =
                repository.getLocal(filters) as Result.Success
            refreshFilters(refreshFilters)
            val assignmentsState = AssignmentsViewState(
                loading = false,
                getAssignmentsForDisplay(assignmentsResult.data),
                filters
            )
            _state.update {
                assignmentsState
            }
        }
    }

    private fun getAssignmentsForDisplay(
        data: List<TaskAssignment>
    ): Map<TextValue, List<TaskAssignmentWrapper>> {
        val onCompletionKey = TextValue.ForKey(LocalizedString.ON_COMPLETION)
        val todo = data.filter { it.progressStatus == ProgressStatus.TODO }
            .map {
                val showCompleteButton = it.member.id == userId
                TaskAssignmentWrapper(it, showCompleteButton)
            }
            .sortedBy { it.assignment.dueDateTime }
            .groupBy {
                if (it.assignment.task.repeatUnit == RepeatUnit.ON_COMPLETE) {
                    onCompletionKey
                } else {
                    TextValue.ForString(getDay(it.assignment.dueDateTime))
                }
            }

        // Move "On Completion" to top
        val onCompletion = todo[onCompletionKey]
        val ordered = if (onCompletion != null) {
            mapOf(onCompletionKey to onCompletion).plus(todo.minus(onCompletionKey))
        } else {
            todo
        }
        return ordered
    }

    private suspend fun refreshFilters(refreshFilters: Boolean) {
        if (filters.isNotEmpty() && !refreshFilters) { // Refresh only if no filters available already
            return
        }
        filters.clear()
        // Always use all existing assignments to create filters
        val assignments = (repository.getLocal(listOf()) as Result.Success).data
        val personFilterItems = assignments
            .map { // Get members for assignments
                it.member
            }
            .distinct() // Remove duplicates
            .map { member -> // Create person filter items
                PersonFilterItem(
                    id = member.id,
                    displayName = member.name,
                    selected = true
                )
            }
            .plus(
                PersonFilterItem(
                    id = Filter.ALL_ID,
                    displayName = "All",
                    selected = true
                )
            )
        if (personFilterItems.count() > 1) {
            val text = TextValue.ForKey(LocalizedString.FILTER_ALL)
            filters.add(PersonFilter(text = text, personFilterItems))
        }
        _state.update {
            it.copy(filters = filters)
        }
    }
}