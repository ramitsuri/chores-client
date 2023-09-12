package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.error.EditTaskError
import com.ramitsuri.choresclient.model.view.EditTaskViewState
import com.ramitsuri.choresclient.model.view.RepeatUnitSelectionItem
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.ContentDownloader
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EditTaskViewModel(
    private val tasksRepository: TasksRepository,
    private val contentDownloader: ContentDownloader,
    private val dispatchers: DispatcherProvider,
    private val clock: Clock,
) : ViewModel(), KoinComponent {
    private val logger: LogHelper by inject()

    private var task: Task? = null
    private val _state: MutableStateFlow<EditTaskViewState> = MutableStateFlow(EditTaskViewState())
    val state: StateFlow<EditTaskViewState> = _state

    private val _taskEdited: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val taskEdited: StateFlow<Boolean> = _taskEdited

    fun setTaskId(taskId: String) {
        viewModelScope.launch {
            task = tasksRepository.getTask(taskId)
            val task = task
            if (task == null) {
                _state.update {
                    it.copy(loading = false, error = EditTaskError.TaskNotFound)
                }
                return@launch
            }
            _state.update { previousState ->
                previousState.copy(
                    taskName = task.name,
                    repeatValue = task.repeatValue,
                    repeatUnits = RepeatUnit.values()
                        .map { repeatUnit ->
                            RepeatUnitSelectionItem(
                                repeatUnit,
                                selected = repeatUnit == task.repeatUnit
                            )
                        },
                    date = task.dueDateTime.date,
                    time = task.dueDateTime.time,
                    rotateMember = task.rotateMember,
                    loading = false,
                    repeatEndDate = task.repeatEndDateTime?.date,
                    now = Clock.System.now(),
                    status = task.status,
                    error = null,
                )
            }
            updateCanEditTask()
        }
    }

    fun editTaskRequested() {
        _state.update {
            it.copy(loading = true)
        }
        val value = _state.value
        if (!value.enableEditTask) {
            return
        }
        val repeatUnit = value.repeatUnits.first { it.getIsSelected() }.toRepeatUnit()

        if (areNewValuesSameAsOriginal(value)) {
            _state.update {
                it.copy(loading = false)
            }
            setTaskEdited()
            return
        }

        val task = task
        if (task == null) {
            _state.update {
                it.copy(loading = false)
            }
            return
        }

        viewModelScope.launch(dispatchers.main) {
            val result = tasksRepository.editTask(
                taskId = task.id,
                name = value.taskName,
                description = null,
                dueDateTime = LocalDateTime(date = value.date, time = value.time),
                repeatValue = value.repeatValue.toLong(),
                repeatUnit = repeatUnit,
                repeatEndDateTime = value.repeatEndDate?.atTime(LocalTime.parse("23:59:59")),
                rotateMember = value.rotateMember,
                status = value.status,
            )
            when (result) {
                is Result.Success -> {
                    // So that any task assignments for the task that was edited, are removed until
                    // new assignments are created
                    contentDownloader.download(now = clock.now(), forceDownload = true)
                    setTaskEdited()
                }

                is Result.Failure -> {
                    logger.v(TAG, "Edit task failed: ${result.error}")
                    _state.update {
                        it.copy(loading = false, error = result.error)
                    }
                }
            }
        }
    }

    fun onErrorShown() {
        _state.update {
            it.copy(error = null)
        }
    }

    fun onTaskNameUpdated(newName: String) {
        _state.update {
            it.copy(taskName = newName)
        }
        updateCanEditTask()
    }

    fun onRepeatValueUpdated(newRepeatValue: String) {
        val repeatValue = newRepeatValue.toIntOrNull() ?: 0
        _state.update {
            it.copy(repeatValue = repeatValue)
        }
    }

    fun onRepeatUnitSelected(newRepeatUnitKeyString: String) {
        if (newRepeatUnitKeyString == RepeatUnit.NONE.key.toString()) {
            onResetRepeatInfo()
        } else {
            _state.update { state ->
                state.copy(
                    repeatUnits = state.repeatUnits.map { item ->
                        item.duplicate(selected = item.getId() == newRepeatUnitKeyString)
                    })
            }
        }
    }

    fun onRotateMemberUpdated() {
        _state.update {
            it.copy(rotateMember = !it.rotateMember)
        }
    }

    fun onRepeatEndDatePicked(dateMillis: Long) {
        _state.update {
            it.copy(
                repeatEndDate = Instant
                    .fromEpochMilliseconds(dateMillis)
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            )
        }
    }

    fun onResetRepeatInfo() {
        val task = task ?: return
        _state.update {
            it.copy(
                repeatValue = task.repeatValue,
                repeatUnits = RepeatUnit.values()
                    .map { repeatUnit ->
                        RepeatUnitSelectionItem(
                            repeatUnit = repeatUnit,
                            selected = repeatUnit == task.repeatUnit
                        )
                    },
                repeatEndDate = task.repeatEndDateTime?.date,
                rotateMember = task.rotateMember,
            )
        }
    }

    fun onDatePicked(dateMillis: Long) {
        _state.update {
            it.copy(
                date = Instant
                    .fromEpochMilliseconds(dateMillis)
                    .toLocalDateTime(TimeZone.UTC)
                    .date
            )
        }
    }

    fun onTimePicked(hour: Int, minute: Int) {
        _state.update {
            it.copy(time = LocalTime(hour = hour, minute = minute))
        }
    }

    fun onActiveStatusSelected(status: ActiveStatus) {
        _state.update {
            it.copy(status = status)
        }
    }

    fun onResetActiveStatus() {
        val task = task ?: return
        _state.update {
            it.copy(status = task.status)
        }
    }

    private fun setTaskEdited() {
        _taskEdited.update {
            true
        }
    }

    private fun updateCanEditTask() {
        val value = _state.value
        val canEdit = value.taskName.isNotEmpty()

        _state.update {
            it.copy(enableEditTask = canEdit)
        }
    }

    private fun areNewValuesSameAsOriginal(value: EditTaskViewState): Boolean {
        return task?.name == value.taskName &&
                task?.repeatValue == value.repeatValue &&
                task?.repeatUnit == value.repeatUnits.first { it.getIsSelected() }.toRepeatUnit() &&
                task?.repeatEndDateTime?.date == value.repeatEndDate &&
                task?.dueDateTime?.date == value.date &&
                task?.dueDateTime?.time == value.time &&
                task?.rotateMember == value.rotateMember &&
                task?.status == value.status
    }

    companion object {
        private const val TAG = "EditTaskViewModel"
    }
}