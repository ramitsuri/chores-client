package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.ActiveStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskDto
import com.ramitsuri.choresclient.data.entities.TaskDao
import com.ramitsuri.choresclient.model.AddEditTaskViewState
import com.ramitsuri.choresclient.model.HouseSelectionItem
import com.ramitsuri.choresclient.model.MemberSelectionItem
import com.ramitsuri.choresclient.model.RepeatUnitSelectionItem
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddEditTaskViewModel(
    private val repository: TasksRepository,
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val taskDao: TaskDao,
    private val syncRepository: SyncRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel(), KoinComponent {

    private val logger: LogHelper by inject()

    private val _state = MutableStateFlow(AddEditTaskViewState())
    val state: StateFlow<AddEditTaskViewState> = _state

    private var taskId: String? = null

    fun setTaskId(taskId: String?) {
        viewModelScope.launch {
            if (taskId == null) {
                _state.update {
                    AddEditTaskViewState()
                }
                populateSelectionItems()
                return@launch
            }
            val task = taskDao.get(taskId) ?: return@launch
            this@AddEditTaskViewModel.taskId = task.id
            _state.update { previousState ->
                previousState.copy(
                    taskName = task.name,
                    taskDescription = task.description,
                    repeatValue = task.repeatValue.toInt(),
                    repeatUnits = RepeatUnit.values()
                        .map { repeatUnit ->
                            RepeatUnitSelectionItem(
                                repeatUnit,
                                selected = repeatUnit == task.repeatUnit
                            )
                        },
                    houses = previousState.houses
                        .map { it.duplicate(selected = it.getId() == task.houseId) },
                    members = previousState.members
                        .map { it.duplicate(selected = it.getId() == task.memberId) },
                    date = task.dueDateTime.date,
                    isDatePicked = true,
                    time = task.dueDateTime.time,
                    isTimePicked = true,
                    rotateMember = task.rotateMember
                )
            }
            updateCanAddEditTask()
        }
    }

    fun saveTask() {
        val value = _state.value
        val repeatUnit =
            RepeatUnit.fromKey(
                value.repeatUnits.firstOrNull { it.getIsSelected() }?.getId()
                    ?.toIntOrNull() ?: 0
            )
        val task = TaskDto(
            name = value.taskName,
            description = value.taskDescription,
            dueDateTime = LocalDateTime(value.date, value.time),
            repeatValue = value.repeatValue,
            repeatUnit = repeatUnit,
            houseId = value.houses.firstOrNull { it.getIsSelected() }?.getId(),
            memberId = value.members.firstOrNull { it.getIsSelected() }?.getId(),
            rotateMember = value.rotateMember,
            status = ActiveStatus.ACTIVE
        )

        logger.d(TAG, "Save task requested: $task")
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch(dispatchers.main) {
            val result = repository.saveTask(taskId, task)
            _state.update {
                when (result) {

                    is Result.Success -> {
                        it.copy(loading = false, taskSaved = true)
                    }

                    is Result.Failure -> {
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
    }

    fun onTaskDescriptionUpdated(newDescription: String) {
        _state.update {
            it.copy(taskDescription = newDescription)
        }
    }

    fun onRepeatValueUpdated(newRepeatValue: String) {
        val repeatValue = newRepeatValue.toIntOrNull() ?: 0
        _state.update {
            it.copy(repeatValue = repeatValue)
        }
    }

    fun onRepeatUnitSelected(newRepeatUnitKeyString: String) {
        _state.update { state ->
            state.copy(repeatUnits = state.repeatUnits.map { item ->
                if (item.getId() == newRepeatUnitKeyString) {
                    item.duplicate(selected = true)
                } else {
                    item.duplicate(selected = false)
                }
            })
        }
    }

    fun onHouseSelected(newHouseId: String) {
        _state.update { state ->
            state.copy(houses = state.houses.map { item ->
                if (item.getId() == newHouseId) {
                    item.duplicate(selected = true)
                } else {
                    item.duplicate(selected = false)
                }
            })
        }
        updateCanAddEditTask()
    }

    fun onMemberSelected(newMemberId: String) {
        _state.update { state ->
            state.copy(members = state.members.map { item ->
                if (item.getId() == newMemberId) {
                    item.duplicate(selected = true)
                } else {
                    item.duplicate(selected = false)
                }
            })
        }
        updateCanAddEditTask()
    }

    fun onDatePicked(newLocalDate: LocalDate) {
        _state.update {
            it.copy(date = newLocalDate, isDatePicked = true)
        }
        updateCanAddEditTask()
    }

    fun onTimePicked(newLocalTime: LocalTime) {
        _state.update {
            it.copy(time = newLocalTime, isTimePicked = true)
        }
        updateCanAddEditTask()
    }

    fun onRotateMemberUpdated(newRotateMemberValue: Boolean) {
        _state.update {
            it.copy(rotateMember = newRotateMemberValue)
        }
    }

    private fun updateCanAddEditTask() {
        val isEditing = taskId != null
        val value = _state.value
        val house = value.houses.firstOrNull { it.getIsSelected() }
        val member = value.members.firstOrNull { it.getIsSelected() }

        val canAddOrEdit = if (isEditing) {
            true
        } else {
            house != null &&
                    member != null &&
                    value.isTimePicked &&
                    value.isDatePicked
        }

        _state.update {
            it.copy(enableSaveTask = canAddOrEdit)
        }
    }

    private suspend fun populateSelectionItems() {
        val assignments = (taskAssignmentsRepository.getLocal(listOf()) as Result.Success).data
        val memberSelectionItems = assignments
            .map { // Get members for assignments
                it.member
            }
            .distinct() // Remove duplicates
            .map { member -> // Create person filter items
                MemberSelectionItem(
                    member = member,
                    selected = false
                )
            }

        val houses = syncRepository.getLocal()
        val houseSelectionItems = houses
            .map { house ->
                HouseSelectionItem(
                    house = house,
                    selected = false
                )
            }

        _state.update {
            it.copy(members = memberSelectionItems, houses = houseSelectionItems)
        }
    }

    companion object {
        private const val TAG = "AddTaskViewModel"
    }
}

