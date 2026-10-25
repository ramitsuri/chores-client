package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.view.ListTypeSelection
import com.ramitsuri.choresclient.model.view.TaskDetails
import com.ramitsuri.choresclient.model.view.TasksViewState
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.TaskUploadRequestHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TasksViewModel(
    tasksRepository: TasksRepository,
    taskUploadRequestHandler: TaskUploadRequestHandler,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedListTypes = MutableStateFlow(ListTypeSelection.entries.toSet())

    val state = combine(
        tasksRepository.getTasksFlow(),
        tasksRepository.getUnsyncedTasksFlow(),
        taskUploadRequestHandler.getUploadStatusFlow(),
        searchQuery,
        selectedListTypes,
    ) { taskDetailsList, unsyncedTaskDetailsList, uploadStatus, query, selectedListTypes ->
        val filteredTasks = taskDetailsList
            .filterByListTypes(selectedListTypes)
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
            searchQuery = query,
            selectedListTypes = selectedListTypes,
            unsyncedTaskCount = unsyncedTaskDetailsList.size,
            isUploading = uploadStatus.isUploading,
            isSyncScheduled = uploadStatus.isScheduled || unsyncedTaskDetailsList.isNotEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksViewState()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onListTypeClicked(listTypeSelection: ListTypeSelection) {
        selectedListTypes.update { currentSelection ->
            if (listTypeSelection in currentSelection) {
                if (currentSelection.size == 1) {
                    // Final selected type is clicked, unselect it + select everything else
                    ListTypeSelection.entries.toSet() - listTypeSelection
                } else {
                    currentSelection - listTypeSelection
                }
            } else {
                currentSelection + listTypeSelection
            }
        }
    }

    fun List<TaskDetails>.filterByListTypes(selectedListTypes: Set<ListTypeSelection>): List<TaskDetails> {
        if (selectedListTypes.size == ListTypeSelection.entries.size) {
            return this
        }
        val isShared = selectedListTypes.contains(ListTypeSelection.SHARED)
        return filter { it.isSharedList == isShared }
    }
}
