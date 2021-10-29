package com.ramitsuri.choresclient.android.ui.assigments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.choresclient.android.model.AssignmentsViewState
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
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
    private var filterMode: FilterMode = FilterMode.ALL

    fun fetchAssignments(getLocal: Boolean = false) {
        val isWorkerRunning = prefManager.isWorkerRunning()
        val shouldGetLocal = getLocal || isWorkerRunning
        Timber.d("Will get local: $shouldGetLocal - getLocal($getLocal) || workerRunning($isWorkerRunning)")
        _state.value = ViewState.Loading
        viewModelScope.launch(dispatchers.main) {
            val assignmentsResult = repository.getTaskAssignments(shouldGetLocal)
            _state.value = when (assignmentsResult) {
                is Result.Failure -> {
                    ViewState.Error(assignmentsResult.error)
                }
                is Result.Success -> {
                    val userId = prefManager.getUserId()
                    val assignmentsState = AssignmentsViewState(
                        getAssignmentsForDisplay(assignmentsResult.data, userId),
                        FilterMode.ALL
                    )
                    ViewState.Success(assignmentsState)
                }
            }
        }
    }

    fun filterAll() {
        filterMode = FilterMode.ALL
        filter()
    }

    fun filterMine() {
        filterMode = FilterMode.MINE(prefManager.getUserId(null) ?: "")
        filter()
    }

    fun filterExceptMine() {
        filterMode = FilterMode.OTHER(prefManager.getUserId(null) ?: "")
        filter()
    }

    private fun filter() {
        viewModelScope.launch(dispatchers.main) {
            val userId = prefManager.getUserId()
            val assignmentsResult = repository.filter(filterMode) as Result.Success
            val assignmentsState = AssignmentsViewState(
                getAssignmentsForDisplay(assignmentsResult.data, userId),
                filterMode
            )
            _state.value = ViewState.Success(assignmentsState)
        }
    }

    private fun getAssignmentsForDisplay(
        data: List<TaskAssignment>,
        userId: String?
    ): List<TaskAssignment> {
        val todo = data.filter {it.progressStatus == ProgressStatus.TODO}
        val forMember = todo.filter {it.member.id == userId}
            .sortedWith(compareBy({it.member.name}, {it.dueDateTime}))
        val forOthers = todo.filter {it.member.id != userId}
            .sortedWith(compareBy({it.member.name}, {it.dueDateTime}))
        return forMember.plus(forOthers)
    }

    fun changeStateRequested(taskAssignment: TaskAssignment, clickType: ClickType) {
        try {
            UUID.fromString(prefManager.getUserId()) ?: return
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
