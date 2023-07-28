package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterItem

class AssignmentsCallbackViewModel(vm: AssignmentsViewModel) : CallbackViewModel() {
    override val viewModel: AssignmentsViewModel = vm

    val state = viewModel.state.asCallbacks()

    fun fetchAssignments(getLocal: Boolean = false) {
        viewModel.fetchAssignments(getLocal)
    }

    fun filter(filter: Filter, filterItem: FilterItem) {
        viewModel.filter(filter, filterItem)
    }

    fun changeStateRequested(id: String, progressStatus: ProgressStatus) {
        viewModel.markAsDone(id, progressStatus)
    }
}