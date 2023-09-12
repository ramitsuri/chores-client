package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.AddTaskViewState
import com.ramitsuri.choresclient.model.view.HouseSelectionItem
import com.ramitsuri.choresclient.model.view.MemberSelectionItem
import com.ramitsuri.choresclient.model.view.RepeatUnitSelectionItem
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AddTaskViewModel(
    private val tasksRepository: TasksRepository,
    private val syncRepository: SyncRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel(), KoinComponent {
    private val logger: LogHelper by inject()

    private val _state = MutableStateFlow(AddTaskViewState())
    val state: StateFlow<AddTaskViewState> = _state

    private val _taskAdded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val taskAdded: StateFlow<Boolean> = _taskAdded

    init {
        viewModelScope.launch {
            populateHouses()
            val selectedHouseId = _state.value.houses.firstOrNull { it.getIsSelected() }?.getId()
            if (selectedHouseId != null) {
                populateMembers(selectedHouseId)
            }
        }
    }

    fun addTaskRequested() {
        _state.update {
            it.copy(loading = true)
        }
        val value = _state.value
        if (!value.enableAddTask) {
            return
        }
        val repeatUnit = value.repeatUnits.first { it.getIsSelected() }.toRepeatUnit()
        val houseId = value.houses.firstOrNull { it.getIsSelected() }?.getId()
        val memberId = value.members.firstOrNull { it.getIsSelected() }?.getId()
        if (houseId == null || memberId == null) {
            return
        }

        viewModelScope.launch(dispatchers.main) {
            val result = tasksRepository.addTask(
                name = value.taskName,
                description = value.taskName,
                dueDateTime = LocalDateTime(date = value.date, time = value.time),
                repeatValue = value.repeatValue.toLong(),
                repeatUnit = repeatUnit,
                repeatEndDateTime = value.repeatEndDate?.atTime(LocalTime.parse("23:59:59")),
                houseId = houseId,
                memberId = memberId,
                rotateMember = value.rotateMember
            )
            when (result) {
                is Result.Success -> {
                    setTaskAdded()
                }

                is Result.Failure -> {
                    logger.v(TAG, "Add task failed: ${result.error}")
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
        updateCanAddTask()
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
        _state.update {
            it.copy(
                repeatValue = 1,
                repeatUnits = RepeatUnit.values()
                    .map { repeatUnit ->
                        RepeatUnitSelectionItem(
                            repeatUnit = repeatUnit,
                            selected = repeatUnit == RepeatUnit.NONE
                        )
                    },
                repeatEndDate = null,
                rotateMember = false,
            )
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
        viewModelScope.launch {
            populateMembers(newHouseId)
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

    private fun setTaskAdded() {
        _taskAdded.update {
            true
        }
    }

    private fun updateCanAddTask() {
        val value = _state.value
        val house = value.houses.firstOrNull { it.getIsSelected() }
        val member = value.members.firstOrNull { it.getIsSelected() }

        val canAdd = house != null &&
                member != null &&
                value.taskName.isNotEmpty()

        _state.update {
            it.copy(enableAddTask = canAdd)
        }
    }

    private suspend fun populateHouses() {
        val houses = syncRepository.getHouses()
        val houseSelectionItems = houses
            .mapIndexed { index, house ->
                HouseSelectionItem(
                    house = house,
                    selected = index == 0
                )
            }

        _state.update {
            it.copy(houses = houseSelectionItems)
        }
    }

    private suspend fun populateMembers(selectedHouseId: String) {
        val members = syncRepository.getMemberHouseAssociationsForHouses(selectedHouseId)
        val memberSelectionItems = members
            .map { it.member }
            .distinct()
            .mapIndexed { index, member ->
                MemberSelectionItem(
                    member = member,
                    selected = index == 0
                )
            }

        _state.update {
            it.copy(members = memberSelectionItems)
        }
    }

    companion object {
        private const val TAG = "AddTaskViewModel"
    }
}