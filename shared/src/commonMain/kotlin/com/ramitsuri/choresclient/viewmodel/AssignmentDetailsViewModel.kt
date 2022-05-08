package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.model.AssignmentDetails
import com.ramitsuri.choresclient.model.AssignmentDetailsViewState
import com.ramitsuri.choresclient.model.ViewEvent
import com.ramitsuri.choresclient.model.ViewState
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AssignmentDetailsViewModel(
    private val assignmentDetailsRepository: AssignmentDetailsRepository
) : ViewModel() {

    private lateinit var details: AssignmentDetails

    private val _state: MutableStateFlow<ViewState<AssignmentDetailsViewState>> =
        MutableStateFlow(ViewState.Event(ViewEvent.LOADING))
    val state: StateFlow<ViewState<AssignmentDetailsViewState>> = _state

    suspend fun setAssignmentId(assignmentId: String) {
        val details = assignmentDetailsRepository.getDetails(assignmentId)
        if (details != null) {
            this.details = details
            _state.update {
                ViewState.Success(AssignmentDetailsViewState(details))
            }
        } else {
            _state.update {
                ViewState.error(ViewError.TASK_ASSIGNMENT_DETAILS_NULL)
            }
        }
    }

    fun onSnoozeHour() {
        if (!this::details.isInitialized) {
            return
        }
        assignmentDetailsRepository.onSnoozeHourRequested(details.id, details.name)
    }

    fun onSnoozeDay() {
        if (!this::details.isInitialized) {
            return
        }
        assignmentDetailsRepository.onSnoozeDayRequested(details.id, details.name)
    }

    fun onComplete() {
        if (!this::details.isInitialized) {
            return
        }
        assignmentDetailsRepository.onCompleteRequested(details.id)
    }
}