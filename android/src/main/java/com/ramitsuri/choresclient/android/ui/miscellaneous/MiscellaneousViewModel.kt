package com.ramitsuri.choresclient.android.ui.miscellaneous

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
import com.ramitsuri.choresclient.android.utils.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MiscellaneousViewModel @Inject constructor(
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider
): ViewModel() {

    private val _state = MutableLiveData<ViewState<List<TaskAssignment>>>(ViewState.Reload)
    val state: LiveData<ViewState<List<TaskAssignment>>> = _state

    fun fetchAssignments() {
        _state.value = ViewState.Loading
        viewModelScope.launch(dispatchers.main) {
            val assignmentsResult = repository.getTaskAssignments(getLocal = true)
            _state.value = when (assignmentsResult) {
                is Result.Failure -> {
                    ViewState.Error(assignmentsResult.error)
                }
                is Result.Success -> {
                    ViewState.Success(
                        getAssignmentsForDisplay(
                            assignmentsResult.data,
                            prefManager.getUserId()
                        )
                    )
                }
            }
        }
    }

    fun userIdSet(userId: String) {
        prefManager.setUserId(userId)
    }

    private fun getAssignmentsForDisplay(
        data: List<TaskAssignment>,
        userId: String?
    ): List<TaskAssignment> {
        val todo = data.filter {it.progressStatus == ProgressStatus.DONE}
        val forMember = todo.filter {it.member.id == userId}
            .sortedWith(compareBy({it.member.name}, {it.dueDateTime}))
        val forOthers = todo.filter {it.member.id != userId}
            .sortedWith(compareBy({it.member.name}, {it.dueDateTime}))
        return forMember.plus(forOthers)
    }
}