package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem

class AssignmentsCallbackViewModel(vm: AssignmentsViewModel) : CallbackViewModel() {
    override val viewModel: AssignmentsViewModel = vm

    val state = viewModel.state.asCallbacks()

    fun fetchAssignments(getLocal: Boolean = false) {
        viewModel.fetchAssignments(getLocal)
    }

    fun filter(filter: Filter, filterItem: FilterItem) {
        viewModel.onFilterItemClicked(filter, filterItem)
    }

    fun changeStateRequested(id: String, progressStatus: ProgressStatus) {
        viewModel.markAsDone(id, progressStatus)
    }
}