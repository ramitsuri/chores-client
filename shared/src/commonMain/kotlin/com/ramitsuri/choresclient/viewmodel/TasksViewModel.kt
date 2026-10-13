package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.view.TasksViewState
import com.ramitsuri.choresclient.repositories.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TasksViewModel(
    tasksRepository: TasksRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val state = combine(
        tasksRepository.getTasksFlow(),
        searchQuery
    ) { taskDetailsList, query ->
        val filteredTasks = taskDetailsList
            .sortedBy { it.task.name }
            .filter {
                it.task.status in listOf(ActiveStatus.ACTIVE, ActiveStatus.PAUSED)
            }
            .filter { details ->
                query.isBlank() ||
                    details.task.name.contains(query, ignoreCase = true) ||
                    details.task.description.contains(query, ignoreCase = true)
            }
        TasksViewState(
            loading = false,
            tasks = filteredTasks,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksViewState()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}
