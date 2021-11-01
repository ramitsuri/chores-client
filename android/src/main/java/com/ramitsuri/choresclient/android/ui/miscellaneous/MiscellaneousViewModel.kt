package com.ramitsuri.choresclient.android.ui.miscellaneous

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
import com.ramitsuri.choresclient.android.ui.assigments.FilterMode
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import com.ramitsuri.choresclient.android.utils.getDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiscellaneousViewModel @Inject constructor(
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
): ViewModel() {

    private val _state = MutableLiveData<ViewState<AssignmentsViewState>>(ViewState.Reload)
    val state: LiveData<ViewState<AssignmentsViewState>> = _state
    private var filterMode: FilterMode
    private var userId = prefManager.getUserId() ?: ""

    init {
        filterMode = if (userId.isNotEmpty()) {
            FilterMode.MINE(userId)
        } else {
            FilterMode.OTHER("")
        }
    }

    fun fetchAssignments() {
        _state.value = ViewState.Loading
        viewModelScope.launch(dispatchers.main) {
            when (val assignmentsResult = repository.getTaskAssignments(true)) {
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

    fun userIdSet(userId: String) {
        prefManager.setUserId(userId)
        this.userId = userId
        filter()
    }

    private fun getAssignmentsForDisplay(
        data: List<TaskAssignment>
    ): List<TaskAssignmentWrapper> {
        val todo = data.filter {it.progressStatus == ProgressStatus.DONE}
            .sortedByDescending {it.dueDateTime}
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
}