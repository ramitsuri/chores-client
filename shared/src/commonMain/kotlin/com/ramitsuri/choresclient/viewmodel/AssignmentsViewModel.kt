package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.FilterMode
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.AssignmentsViewState
import com.ramitsuri.choresclient.model.TaskAssignmentWrapper
import com.ramitsuri.choresclient.model.ViewEvent
import com.ramitsuri.choresclient.model.ViewState
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
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
    private val _state: MutableStateFlow<ViewState<AssignmentsViewState>> =
        MutableStateFlow(
            ViewState.Event(ViewEvent.RELOAD)
        )

    val state: StateFlow<ViewState<AssignmentsViewState>> = _state

    private var filterMode: FilterMode
    private val userId = prefManager.getUserId() ?: ""

    init {
        filterMode = if (userId.isNotEmpty()) {
            FilterMode.MINE(userId)
        } else {
            FilterMode.OTHER("")
        }
    }

    fun fetchAssignments(getLocal: Boolean = false) {
        val isWorkerRunning = appHelper.isWorkerRunning()
        val shouldRefresh = !(getLocal || isWorkerRunning)
        logger.d(
            "AssignmentsViewModel",
            "Will refresh: $shouldRefresh - getLocal($getLocal) || workerRunning($isWorkerRunning)"
        )
        _state.update {
            ViewState.Event(ViewEvent.LOADING)
        }
        // We want this to be run in long living scope so that the refresh operation isn't cancelled
        // while assignments have been uploaded but not deleted locally for example. Or are being
        // uploaded still
        longLivingCoroutineScope.launch(dispatchers.main) {
            if (shouldRefresh) {
                repository.refresh()
                getLocal()
            } else {
                getLocal()
            }
        }
    }

    fun filterMine() {
        filterMode = FilterMode.MINE(userId)
        getLocal()
    }

    fun filterExceptMine() {
        filterMode = FilterMode.OTHER(userId)
        getLocal()
    }

    fun changeStateRequested(taskAssignment: TaskAssignment) {
        if (taskAssignment.progressStatus != ProgressStatus.TODO) {
            return
        }
        longLivingCoroutineScope.launch {
            assignmentDetailsRepository.onCompleteRequestedSuspend(taskAssignment.id)
            getLocal()
        }
    }

    fun toggleLogging() {
        val currentlyEnabled = prefManager.getEnableRemoteLogging()
        prefManager.setEnableRemoteLogging(!currentlyEnabled)
        logger.enableRemoteLogging(!currentlyEnabled)
    }

    private fun getLocal() {
        viewModelScope.launch(dispatchers.main) {
            val assignmentsResult =
                repository.getLocal(filterMode) as Result.Success
            val assignmentsState = AssignmentsViewState(
                getAssignmentsForDisplay(assignmentsResult.data),
                filterMode
            )
            _state.update {
                ViewState.Success(assignmentsState)
            }
        }
    }

    private fun getAssignmentsForDisplay(
        data: List<TaskAssignment>
    ): List<TaskAssignmentWrapper> {
        val onCompletionKey = "On Completion"
        val todo = data.filter { it.progressStatus == ProgressStatus.TODO }
            .sortedBy { it.dueDateTime }
            .groupBy {
                if (it.task.repeatUnit == RepeatUnit.ON_COMPLETE) {
                    onCompletionKey
                } else {
                    getDay(it.dueDateTime)
                }
            }

        // Move "On Completion" to top
        val onCompletion = todo[onCompletionKey]
        val ordered = if (onCompletion != null) {
            mapOf(onCompletionKey to onCompletion).plus(todo.minus(onCompletionKey))
        } else {
            todo
        }

        val result = mutableListOf<TaskAssignmentWrapper>()
        for ((date, assignmentsForDate) in ordered) {
            result.add(TaskAssignmentWrapper(headerView = date))
            for (assignment in assignmentsForDate) {
                result.add(TaskAssignmentWrapper(itemView = assignment))
            }
        }
        return result
    }
}