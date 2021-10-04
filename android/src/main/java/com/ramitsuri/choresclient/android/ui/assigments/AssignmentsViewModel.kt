package com.ramitsuri.choresclient.android.ui.assigments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val repository: TaskAssignmentsRepository,
    private val dispatchers: DispatcherProvider
): ViewModel() {

    private val _state = MutableLiveData<ViewState<List<TaskAssignment>>>(ViewState.Reload)
    val state: LiveData<ViewState<List<TaskAssignment>>> = _state

    fun fetchAssignments() {
        _state.value = ViewState.Loading
        viewModelScope.launch(dispatchers.main) {
            val assignmentsResult = repository.getTaskAssignments()
            _state.value = when (assignmentsResult) {
                is Result.Failure -> {
                    ViewState.Error(assignmentsResult.error)
                }
                is Result.Success -> {
                    ViewState.Success(getFilteredAssignments(assignmentsResult.data))
                }
            }
        }
    }

    private fun getFilteredAssignments(data: List<TaskAssignment>): List<TaskAssignment> {
        return data.filter {it.progressStatus == ProgressStatus.TODO}
    }

    fun changeStateRequested(taskAssignment: TaskAssignment, clickType: ClickType) {
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
            val result =
                repository.saveTaskAssignments(taskAssignment.id, newProgressStatus)
            _state.value = when (result) {
                is Result.Failure -> {
                    ViewState.Error(result.error)
                }
                is Result.Success -> {
                    ViewState.Reload
                }
            }
        }
    }
}
