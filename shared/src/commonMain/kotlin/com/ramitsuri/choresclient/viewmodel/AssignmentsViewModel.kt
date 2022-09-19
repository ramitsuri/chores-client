package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.AssignmentsViewState
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterItem
import com.ramitsuri.choresclient.model.TaskAssignmentWrapper
import com.ramitsuri.choresclient.model.TextValue
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.resources.LocalizedString
import com.ramitsuri.choresclient.utils.AppHelper
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.FilterHelper
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
    private val filterHelper: FilterHelper,
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
        val newFilter = filterHelper.onFilterItemSelected(filter, filterItem)
        filters.removeAll { it.getType() == filter.getType() }
        filters.add(newFilter)
        filters.sortBy { it.getType().index }
        _state.update {
            it.copy(filters = filters, loading = true)
        }
        getLocal(refreshFilters = false)
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
            refreshFilters(refreshFilters)
            val assignmentsResult =
                repository.getLocal(filters) as Result.Success
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
        filters.addAll(filterHelper.get())
        filters.sortBy { it.getType().index }
        _state.update {
            it.copy(filters = filters)
        }
    }
}