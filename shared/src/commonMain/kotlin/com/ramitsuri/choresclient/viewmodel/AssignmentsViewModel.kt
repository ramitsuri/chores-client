package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem
import com.ramitsuri.choresclient.model.filter.FilterType
import com.ramitsuri.choresclient.model.view.Assignments
import com.ramitsuri.choresclient.model.view.AssignmentsViewState
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.FilterHelper
import com.ramitsuri.choresclient.utils.differenceInDays
import com.ramitsuri.choresclient.utils.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class AssignmentsViewModel(
    assignmentId: String?,
    private val repository: TaskAssignmentsRepository,
    private val filterHelper: FilterHelper,
    private val prefManager: PrefManager,
    private val longLivingCoroutineScope: CoroutineScope
) : ViewModel() {

    private var collectJob: Job? = null

    private val _state = MutableStateFlow(AssignmentsViewState(expandedAssignmentId = assignmentId))
    val state: StateFlow<AssignmentsViewState> = _state

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(filters = filterHelper.getBaseFilters())
            }
            onFilterUpdated()
        }
    }

    fun onFilterItemClicked(filter: Filter, clickedFilterItem: FilterItem) {
        val existingFilters = _state.value.filters
        val newClickedFilter = filterHelper.onFilterItemClicked(filter, clickedFilterItem)
        val newFilters = existingFilters
            .filter {
                it.getType() != filter.getType()
            }
            .plus(newClickedFilter)
            .sortedBy { it.getType().index }
        _state.update {
            it.copy(filters = newFilters)
        }
        onFilterUpdated()
    }

    fun markAsDone(id: String) {
        longLivingCoroutineScope.launch {
            repository.markTaskAssignmentDone(id, Clock.System.now())
        }
    }

    fun markAsWontDo(id: String) {
        longLivingCoroutineScope.launch {
            repository.markTaskAssignmentWontDo(id, Clock.System.now())
        }
    }

    fun onSnooze(id: String, type: SnoozeType) {
        longLivingCoroutineScope.launch {
            repository.onSnoozeRequested(id, type)
        }
    }

    fun onCustomSnoozeHoursEntered(hours: String) {
        if (hours.any { !it.isDigit() }) {
            return
        }
        if ((hours.toIntOrNull() ?: 0) > 23) {
            return
        }
        _state.update {
            it.copy(customSnoozeHours = hours)
        }
    }

    fun onCustomSnoozeMinutesEntered(minutes: String) {
        if (minutes.any { !it.isDigit() }) {
            return
        }
        if ((minutes.toIntOrNull() ?: 0) > 59) {
            return
        }
        _state.update {
            it.copy(customSnoozeMinutes = minutes)
        }
    }

    fun onCustomSnoozeSet(id: String) {
        val currentState = _state.value
        val hours = currentState.customSnoozeHours.toIntOrNull() ?: 0
        val minutes = currentState.customSnoozeMinutes.toIntOrNull() ?: 0
        onSnooze(id, SnoozeType.Custom(hours.hours + minutes.minutes))
        // To reset the state values for next invocation of custom snooze view
        onCustomSnoozeCanceled()
    }

    fun onCustomSnoozeCanceled() {
        _state.update {
            it.copy(customSnoozeHours = "", customSnoozeMinutes = "")
        }
    }

    fun onItemClicked(assignmentId: String) {
        val currentState = _state.value
        if (currentState.expandedAssignmentId == assignmentId) { // Collapse if already expanded
            _state.update {
                it.copy(expandedAssignmentId = null)
            }
        } else {
            _state.update {
                it.copy(expandedAssignmentId = assignmentId)
            }
        }
    }

    private fun onFilterUpdated() {
        collectJob?.cancel()

        val loggedInMemberId = prefManager.getLoggedInMemberId() ?: ""

        val filters = _state.value.filters
        collectJob = viewModelScope.launch {
            repository.getLocalFlow(loggedInMemberId).collect { taskAssignmentDetails ->
                val filtered = taskAssignmentDetails
                    .filter { it.taskAssignment.progressStatus == ProgressStatus.TODO }
                    .toMutableList()
                filters.forEach { filter ->
                    when (filter.getType()) {
                        FilterType.PERSON -> {
                            if (filter.getItems()
                                    .any { it.getIsSelected() && it.getId() == Filter.ALL_ID }
                            ) {
                                // All selected, remove nothing
                            } else {
                                val selectedMemberIds =
                                    filter.getItems().filter { it.getIsSelected() }
                                        .map { it.getId() }
                                filtered
                                    .removeAll {
                                        !selectedMemberIds.contains(it.taskAssignment.memberId)
                                    }
                            }
                        }

                        FilterType.HOUSE -> {
                            if (filter.getItems()
                                    .any { it.getIsSelected() && it.getId() == Filter.ALL_ID }
                            ) {
                                // All selected, remove nothing
                            } else {
                                val selectedHouseIds =
                                    filter.getItems().filter { it.getIsSelected() }
                                        .map { it.getId() }
                                filtered
                                    .removeAll {
                                        !selectedHouseIds.contains(it.taskAssignment.houseId)
                                    }
                            }
                        }
                    }
                }
                // Apply sorting
                val (onCompletion, notOnCompletion) = filtered
                    .partition {
                        it.taskAssignment.repeatInfo.repeatUnit == RepeatUnit.ON_COMPLETE
                    }

                val assignments = onCompletion
                    .plus(notOnCompletion
                        .sortedBy {
                            it.taskAssignment.dueDateTime
                        }
                    )
                    .toAssignments(prefManager.getLoggedInMemberId())

                _state.update {
                    it.copy(
                        loading = false,
                        assignments = assignments
                    )
                }
            }
        }
    }

    private fun List<TaskAssignmentDetails>.toAssignments(
        loggedInMemberId: String?,
        now: LocalDateTime = LocalDateTime.now()
    ): Assignments {

        val assignments = mutableListOf<TaskAssignmentDetails>()
        val counts = mutableMapOf<String, Int>()
        val grouped = filter { it.taskAssignment.progressStatus == ProgressStatus.TODO }
            .groupBy { it.taskAssignment.taskId }
        grouped.forEach { (_, assignmentsForTask) ->
            val (ownAssignments, othersAssignments) = assignmentsForTask
                .partition { it.taskAssignment.memberId == loggedInMemberId }

            val ownAssignmentToShow =
                ownAssignments.minByOrNull { it.taskAssignment.dueDateTime }
            if (ownAssignmentToShow != null) {
                assignments.add(ownAssignmentToShow)
                counts[ownAssignmentToShow.taskAssignment.id] = ownAssignments.size - 1
            }

            val othersAssignmentToShow =
                othersAssignments.minByOrNull { it.taskAssignment.dueDateTime }
            if (othersAssignmentToShow != null) {
                assignments.add(othersAssignmentToShow)
                counts[othersAssignmentToShow.taskAssignment.id] = othersAssignments.size - 1
            }
        }

        // Move "On Completion" to top
        val (onCompletion, others) = assignments
            .partition { it.taskAssignment.repeatInfo.repeatUnit == RepeatUnit.ON_COMPLETE }

        val pastDue = mutableListOf<TaskAssignmentDetails>()
        val dueToday = mutableListOf<TaskAssignmentDetails>()
        val dueTomorrow = mutableListOf<TaskAssignmentDetails>()
        val dueInFuture = mutableListOf<TaskAssignmentDetails>()
        others
            .sortedBy { it.taskAssignment.dueDateTime }
            .forEach { taskAssignment ->
                val difference =
                    differenceInDays(taskAssignment.taskAssignment.dueDateTime, now)
                if (difference < 0) {
                    pastDue.add(taskAssignment)
                } else if (difference == 0) {
                    dueToday.add(taskAssignment)
                } else if (difference == 1) {
                    dueTomorrow.add(taskAssignment)
                } else {
                    dueInFuture.add(taskAssignment)
                }
            }
        return Assignments(
            onCompletion = onCompletion,
            pastDue = pastDue,
            dueToday = dueToday,
            dueTomorrow = dueTomorrow,
            dueInFuture = dueInFuture,
            otherAssignmentsCount = counts
        )
    }
}