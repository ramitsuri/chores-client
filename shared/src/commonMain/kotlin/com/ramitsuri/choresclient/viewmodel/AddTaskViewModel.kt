package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.ActiveStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Result
import com.ramitsuri.choresclient.data.TaskDto
import com.ramitsuri.choresclient.model.AddTaskViewState
import com.ramitsuri.choresclient.model.HouseSelectionItem
import com.ramitsuri.choresclient.model.MemberSelectionItem
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

class AddTaskViewModel(
    private val repository: TasksRepository,
    private val taskAssignmentsRepository: TaskAssignmentsRepository,
    private val syncRepository: SyncRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel(), KoinComponent {

    private val logger: LogHelper by inject()

    private val _state = MutableStateFlow(AddTaskViewState())
    val state: StateFlow<AddTaskViewState> = _state

    init {
        viewModelScope.launch(dispatchers.io) {
            populateSelectionItems()
        }
    }

    fun addTask() {
        val value = _state.value
        val house = value.houses.firstOrNull { it.getIsSelected() }
        val member = value.members.firstOrNull { it.getIsSelected() }
        if (house == null ||
            member == null ||
            !value.isTimePicked ||
            !value.isDatePicked
        ) {
            return
        }
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
            houseId = house.getId(),
            memberId = member.getId(),
            rotateMember = true,
            status = ActiveStatus.ACTIVE
        )

        logger.d(TAG, "Add task requested: $task")
        _state.update {
            it.copy(loading = true)
        }
        viewModelScope.launch(dispatchers.main) {
            val result = repository.addTask(task)
            _state.update {
                when (result) {

                    is Result.Success -> {
                        it.copy(loading = false, taskAdded = true)
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
        updateCanAddTask()
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
        updateCanAddTask()
    }

    fun onDatePicked(newLocalDate: LocalDate) {
        _state.update {
            it.copy(date = newLocalDate, isDatePicked = true)
        }
        updateCanAddTask()
    }

    fun onTimePicked(newLocalTime: LocalTime) {
        _state.update {
            it.copy(time = newLocalTime, isTimePicked = true)
        }
        updateCanAddTask()
    }

    fun onRotateMemberUpdated(newRotateMemberValue: Boolean) {
        _state.update {
            it.copy(rotateMember = newRotateMemberValue)
        }
    }

    private fun updateCanAddTask() {
        val value = _state.value
        val house = value.houses.firstOrNull { it.getIsSelected() }
        val member = value.members.firstOrNull { it.getIsSelected() }
        if (house == null ||
            member == null ||
            !value.isTimePicked ||
            !value.isDatePicked
        ) {
            _state.update {
                it.copy(enableAddTask = false)
            }
        } else {
            _state.update {
                it.copy(enableAddTask = true)
            }
        }
    }

    private suspend fun populateSelectionItems() {
        val assignments = (taskAssignmentsRepository.getLocal(listOf()) as Result.Success).data
        val houses = syncRepository.getLocal()
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

        val houseSelectionItems = assignments
            .map {
                it.task.houseId
            }
            .distinct()
            .mapNotNull { houseId ->
                houses.find { house ->
                    house.id == houseId
                }
            }
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

