package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.FilterMode
import com.ramitsuri.choresclient.data.ProgressStatus

class AssignmentsCallbackViewModel(vm: AssignmentsViewModel) : CallbackViewModel() {
    override val viewModel: AssignmentsViewModel = vm

    val state = viewModel.state.asCallbacks()

    fun fetchAssignments(getLocal: Boolean = false) {
        viewModel.fetchAssignments(getLocal)
    }

    fun filter(filterMode: FilterMode) {
        viewModel.filter(filterMode)
    }

    fun changeStateRequested(id: String, progressStatus: ProgressStatus) {
        viewModel.changeStateRequested(id, progressStatus)
    }
}