package com.ramitsuri.choresclient.viewmodel

import kotlinx.coroutines.launch

class AssignmentDetailsCallbackViewModel(vm: AssignmentDetailsViewModel) : CallbackViewModel() {
    override val viewModel: AssignmentDetailsViewModel = vm

    val state = viewModel.state.asCallbacks()

    fun setAssignmentId(assignmentId: String) {
        viewModel.viewModelScope.launch {
            viewModel.setAssignmentId(assignmentId)
        }
    }
}