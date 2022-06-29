package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.model.AssignmentDetails
import com.ramitsuri.choresclient.model.AssignmentDetailsViewState
import com.ramitsuri.choresclient.repositories.AssignmentDetailsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AssignmentDetailsViewModel(
    private val assignmentDetailsRepository: AssignmentDetailsRepository
) : ViewModel() {

    private lateinit var details: AssignmentDetails

    private val _state: MutableStateFlow<AssignmentDetailsViewState> =
        MutableStateFlow(AssignmentDetailsViewState(loading = true, assignment = null))
    val state: StateFlow<AssignmentDetailsViewState> = _state

    suspend fun setAssignmentId(assignmentId: String) {
        _state.update {
            AssignmentDetailsViewState(loading = true, assignment = null)
        }
        val details = assignmentDetailsRepository.getDetails(assignmentId)
        if (details != null) {
            this.details = details
            _state.update {
                it.copy(loading = false, assignment = details)
            }
        } else {
            _state.update {
                it.copy(loading = false, assignment = null)
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