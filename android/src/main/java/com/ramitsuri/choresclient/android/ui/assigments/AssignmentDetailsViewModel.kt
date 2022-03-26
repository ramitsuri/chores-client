package com.ramitsuri.choresclient.android.ui.assigments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramitsuri.choresclient.android.model.AssignmentDetailsViewState
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.ViewError
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.repositories.AssignmentActionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    private val assignmentActionManager: AssignmentActionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskAssignment: TaskAssignment? = savedStateHandle["taskAssignment"]
    private val _state: MutableLiveData<ViewState<AssignmentDetailsViewState>> =
        if (taskAssignment != null) {
            MutableLiveData(ViewState.Success(AssignmentDetailsViewState(taskAssignment)))
        } else {
            MutableLiveData(ViewState.Error(ViewError.TASK_ASSIGNMENT_ARG_NULL))
        }
    val state: LiveData<ViewState<AssignmentDetailsViewState>> = _state

    fun onSnoozeHour() {
        if (taskAssignment == null) {
            return
        }
        assignmentActionManager.onSnoozeHourRequested(taskAssignment.id, taskAssignment.task.name)
    }

    fun onSnoozeDay() {
        if (taskAssignment == null) {
            return
        }
        assignmentActionManager.onSnoozeDayRequested(taskAssignment.id, taskAssignment.task.name)
    }

    fun onComplete() {
        if (taskAssignment == null) {
            return
        }
        assignmentActionManager.onCompleteRequested(taskAssignment.id)
    }
}