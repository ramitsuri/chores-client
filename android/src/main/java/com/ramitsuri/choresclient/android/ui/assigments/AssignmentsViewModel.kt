package com.ramitsuri.choresclient.android.ui.assigments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.choresclient.android.model.AssignmentsViewState
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.Result
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.model.TaskAssignmentWrapper
import com.ramitsuri.choresclient.android.model.ViewState
import com.ramitsuri.choresclient.android.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DispatcherProvider
import com.ramitsuri.choresclient.android.utils.PrefManager
import com.ramitsuri.choresclient.android.utils.getDay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class AssignmentsViewModel @Inject constructor(
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager,
    private val dispatchers: DispatcherProvider,
    private val longLivingCoroutineScope: CoroutineScope
) : ViewModel() {

    private val _state = MutableLiveData<ViewState<AssignmentsViewState>>(ViewState.Reload)
    val state: LiveData<ViewState<AssignmentsViewState>> = _state
    private var filterMode: FilterMode
    private val userId = prefManager.getUserId() ?: ""

    init {
        filterMode = if (userId.isNotEmpty()) {
            FilterMode.MINE(userId)
        } else {
            FilterMode.OTHER("")
        }
    }

    fun fetchAssignments(getLocal: Boolean = false) {
        val isWorkerRunning = prefManager.isWorkerRunning()
        val shouldRefresh = !(getLocal || isWorkerRunning)
        Timber.d("Will refresh: $shouldRefresh - getLocal($getLocal) || workerRunning($isWorkerRunning)")
        _state.value = ViewState.Loading
        // We want this to be run in long living scope so that the refresh operation isn't cancelled
        // while assignments have been uiploaded but not deleted locally for example. Or are being
        // uploaded still
        longLivingCoroutineScope.launch(dispatchers.main) {
            if (shouldRefresh) {
                repository.refresh()
                getLocal()
            } else {
                getLocal()
            }
        }
    }

    fun filterMine() {
        filterMode = FilterMode.MINE(userId)
        getLocal()
    }

    fun filterExceptMine() {
        filterMode = FilterMode.OTHER(userId)
        getLocal()
    }

    private fun getLocal() {
        viewModelScope.launch(dispatchers.main) {
            val assignmentsResult = repository.getLocal(filterMode) as Result.Success
            val assignmentsState = AssignmentsViewState(
                getAssignmentsForDisplay(assignmentsResult.data),
                filterMode
            )
            _state.value = ViewState.Success(assignmentsState)
        }
    }

    private fun getAssignmentsForDisplay(
        data: List<TaskAssignment>
    ): List<TaskAssignmentWrapper> {
        val onCompletionKey = "On Completion"
        val todo = data.filter { it.progressStatus == ProgressStatus.TODO }
            .sortedBy { it.dueDateTime }
            .groupBy {
                if (it.task.repeatUnit == RepeatUnit.ON_COMPLETE) {
                    onCompletionKey
                } else {
                    getDay(it.dueDateTime)
                }
            }

        // Move "On Completion" to top
        val onCompletion = todo[onCompletionKey]
        val ordered = if (onCompletion != null) {
            mapOf(onCompletionKey to onCompletion).plus(todo.minus(onCompletionKey))
        } else {
            todo
        }

        val result = mutableListOf<TaskAssignmentWrapper>()
        for ((date, assignmentsForDate) in ordered) {
            result.add(TaskAssignmentWrapper(headerView = date))
            for (assignment in assignmentsForDate) {
                result.add(TaskAssignmentWrapper(itemView = assignment))
            }
        }
        return result
    }

    fun changeStateRequested(taskAssignment: TaskAssignment, clickType: ClickType) {
        val newProgressStatus = when (taskAssignment.progressStatus) {
            ProgressStatus.TODO -> {
                when (clickType) {
                    ClickType.CHANGE_STATUS -> {
                        ProgressStatus.DONE
                    }
                    else -> {
                        return
                    }
                }
            }
            else -> {
                return
            }
        }
        val updated = taskAssignment.copy(
            progressStatus = newProgressStatus,
            progressStatusDate = Instant.now()
        )
        viewModelScope.launch {
            repository.updateTaskAssignment(updated, true)
            getLocal()
        }
    }
}
