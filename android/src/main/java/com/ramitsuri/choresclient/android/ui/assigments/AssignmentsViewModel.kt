package com.ramitsuri.choresclient.android.ui.assigments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.choresclient.android.model.AssignmentsViewState
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.TaskAssignmentWrapper
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import com.ramitsuri.choresclient.android.utils.getDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
): ViewModel() {

    private val _state = MutableLiveData<ViewState<AssignmentsViewState>>(ViewState.Reload)
    val state: LiveData<ViewState<AssignmentsViewState>> = _state
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
        val isWorkerRunning = prefManager.isWorkerRunning()
        val shouldGetLocal = getLocal || isWorkerRunning
        Timber.d("Will get local: $shouldGetLocal - getLocal($getLocal) || workerRunning($isWorkerRunning)")
        _state.value = ViewState.Loading
        viewModelScope.launch(dispatchers.main) {
            when (val assignmentsResult = repository.getTaskAssignments(shouldGetLocal)) {
                is Result.Failure -> {
                    _state.value = ViewState.Error(assignmentsResult.error)
                }
                is Result.Success -> {
                    filter()
                }
            }
        }
    }

    fun filterMine() {
        filterMode = FilterMode.MINE(userId)
        filter()
    }

    fun filterExceptMine() {
        filterMode = FilterMode.OTHER(userId)
        filter()
    }

    private fun filter() {
        viewModelScope.launch(dispatchers.main) {
            val assignmentsResult = repository.filter(filterMode) as Result.Success
            val assignmentsState = AssignmentsViewState(
                getAssignmentsForDisplay(assignmentsResult.data),
                filterMode
            )
            _state.value = ViewState.Success(assignmentsState)
        }
    }

    private fun getAssignmentsForDisplay(
        data: List<TaskAssignment>
    ): List<TaskAssignmentWrapper> {
        val todo = data.filter {it.progressStatus == ProgressStatus.TODO}
            .sortedBy {it.dueDateTime}
            .groupBy {getDay(it.dueDateTime)}

        val result = mutableListOf<TaskAssignmentWrapper>()
        for ((date, assignmentsForDate) in todo) {
            result.add(TaskAssignmentWrapper(headerView = date))
            for (assignment in assignmentsForDate) {
                result.add(TaskAssignmentWrapper(itemView = assignment))
            }
        }
        return result
    }

    fun changeStateRequested(taskAssignment: TaskAssignment, clickType: ClickType) {
        try {
            UUID.fromString(userId) ?: return
        } catch (e: Exception) {
            return
        }
        val newProgressStatus = when (taskAssignment.progressStatus) {
            ProgressStatus.TODO -> {
                when (clickType) {
                    ClickType.CHANGE_STATUS -> {
                        ProgressStatus.DONE
                    }
                    else -> {
                        ProgressStatus.UNKNOWN
                    }
                }
            }
            else -> {
                ProgressStatus.UNKNOWN
            }
        }
        if (newProgressStatus == ProgressStatus.UNKNOWN) {
            return
        }
        _state.value = ViewState.Loading
        viewModelScope.launch {
            when (val result =
                repository.updateTaskAssignment(taskAssignment.id, newProgressStatus)) {
                is Result.Failure -> {
                    _state.value = ViewState.Error(result.error)
                }
                is Result.Success -> {
                    filter()
                }
            }
        }
    }
}
