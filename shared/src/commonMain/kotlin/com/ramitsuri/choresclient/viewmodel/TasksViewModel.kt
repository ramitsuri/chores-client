package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.model.view.TasksViewState
import com.ramitsuri.choresclient.repositories.TasksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TasksViewModel(
    private val tasksRepository: TasksRepository,
) : ViewModel() {


    val state = tasksRepository.getTasksFlow().map { taskDetailsList ->
        TasksViewState(
            loading = false,
            tasks = taskDetailsList.sortedBy { it.task.name }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksViewState()
    )
}
