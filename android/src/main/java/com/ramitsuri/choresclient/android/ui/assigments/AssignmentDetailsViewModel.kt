package com.ramitsuri.choresclient.android.ui.assigments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramitsuri.choresclient.android.model.AssignmentDetailsViewState
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.repositories.AssignmentActionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    private val assignmentActionManager: AssignmentActionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val details: AssignmentDetails? = savedStateHandle["details"]
    private val _state: MutableLiveData<ViewState<AssignmentDetailsViewState>> =
        if (details != null) {
            MutableLiveData(ViewState.Success(AssignmentDetailsViewState(details)))
        } else {
            MutableLiveData(ViewState.Error(ViewError.TASK_ASSIGNMENT_ARG_NULL))
        }
    val state: LiveData<ViewState<AssignmentDetailsViewState>> = _state

    fun onSnoozeHour() {
        if (details == null) {
            return
        }
        assignmentActionManager.onSnoozeHourRequested(details.id, details.name)
    }

    fun onSnoozeDay() {
        if (details == null) {
            return
        }
        assignmentActionManager.onSnoozeDayRequested(details.id, details.name)
    }

    fun onComplete() {
        if (details == null) {
            return
        }
        assignmentActionManager.onCompleteRequested(details.id)
    }
}