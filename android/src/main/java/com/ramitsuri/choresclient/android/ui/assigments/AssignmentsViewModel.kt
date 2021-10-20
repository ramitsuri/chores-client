package com.ramitsuri.choresclient.android.ui.assigments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.notification.ReminderScheduler
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val repository: TaskAssignmentsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val prefManager: PrefManager,
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
                    val userId = prefManager.getUserId()
                    reminderScheduler.addReminders(
                        getAssignmentsForReminders(
                            assignmentsResult.data,
                            userId
                        )
                    )
                    ViewState.Success(getAssignmentsForDisplay(assignmentsResult.data, userId))
                }
            }
        }
    }

    fun filterAll() {
        filter(FilterMode.ALL)
    }

    fun filterMine() {
        filter(FilterMode.MINE(prefManager.getUserId(null) ?: ""))
    }

    fun filterExceptMine() {
        filter(FilterMode.OTHER(prefManager.getUserId(null) ?: ""))
    }

    private fun filter(filterMode: FilterMode) {
        viewModelScope.launch(dispatchers.main) {
            val userId = prefManager.getUserId()
            val assignmentsResult = repository.filter(filterMode) as Result.Success
            _state.value =
                ViewState.Success(getAssignmentsForDisplay(assignmentsResult.data, userId))
        }
    }

    private fun getAssignmentsForReminders(
        data: List<TaskAssignment>,
        userId: String?
    ): List<TaskAssignment> {
        return data
            .filter {it.member.id == userId}
            .filter {it.progressStatus == ProgressStatus.TODO}
            .filter {it.task.repeatUnit != RepeatUnit.ON_COMPLETE}
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
