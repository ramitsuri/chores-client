package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.TaskAssignment

class AssignmentsCallbackViewModel(vm: AssignmentsViewModel) : CallbackViewModel() {
    override val viewModel: AssignmentsViewModel = vm

    val state = viewModel.state.asCallbacks()

    fun fetchAssignments(getLocal: Boolean = false) {
        viewModel.fetchAssignments(getLocal)
    }

    fun filterMine() {
        viewModel.filterMine()
    }

    fun filterExceptMine() {
        viewModel.filterExceptMine()
    }

    fun changeStateRequested(taskAssignment: TaskAssignment) {
        viewModel.changeStateRequested(taskAssignment)
    }
}