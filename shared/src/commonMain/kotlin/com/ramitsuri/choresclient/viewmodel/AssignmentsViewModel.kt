package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.Assignments
import com.ramitsuri.choresclient.model.view.AssignmentsViewState
import com.ramitsuri.choresclient.model.view.ListTypeSelection
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.model.view.filterByListTypes
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.differenceInDays
import com.ramitsuri.choresclient.utils.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock

class AssignmentsViewModel(
    assignmentId: String?,
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager,
    private val longLivingCoroutineScope: CoroutineScope
) : ViewModel() {

    private val selectedListTypes = MutableStateFlow(ListTypeSelection.entries.toSet())

    val state = getAssignments().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AssignmentsViewState(),
    )

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

    private fun getAssignments(): Flow<AssignmentsViewState> {
        val loggedInMemberId = prefManager.getLoggedInMemberId()
        return if (loggedInMemberId == null) {
            flowOf(AssignmentsViewState(loading = false))
        } else {
            combine(
                repository.getLocalFlow(loggedInMemberId),
                selectedListTypes,
            ) { taskAssignmentDetails, selectedListTypes ->
                val filtered = taskAssignmentDetails
                    .filterByListTypes(selectedListTypes)
                    .filter { it.taskAssignment.progressStatus == ProgressStatus.TODO }
                    .toMutableList()

                // Apply sorting
                val (onCompletion, notOnCompletion) = filtered
                    .partition {
                        it.taskAssignment.repeatInfo.repeatUnit == RepeatUnit.ON_COMPLETE
                    }

                val assignments = onCompletion
                    .plus(
                        notOnCompletion
                            .sortedBy {
                                it.taskAssignment.dueDateTime
                            }
                    )
                    .toAssignments(prefManager.getLoggedInMemberId())

                AssignmentsViewState(
                    loading = false,
                    assignments = assignments,
                    selectedListTypes = selectedListTypes,
                )
            }
        }
    }

    private fun List<TaskAssignmentDetails>.toAssignments(
        loggedInMemberId: String?,
        now: LocalDateTime = LocalDateTime.now()
    ): Assignments {
        val todoAssignments = filter { it.taskAssignment.progressStatus == ProgressStatus.TODO }
        val counts = mutableMapOf<String, Int>()

        // 1. On Completion
        val (onCompletionAssignments, timedAssignments) = todoAssignments.partition {
            it.taskAssignment.repeatInfo.repeatUnit == RepeatUnit.ON_COMPLETE
        }

        val onCompletion = mutableListOf<TaskAssignmentDetails>()
        onCompletionAssignments.groupBy { it.taskAssignment.taskId }.forEach { (_, assignmentsForTask) ->
            val (ownAssignments, othersAssignments) = assignmentsForTask
                .partition { it.taskAssignment.memberId == loggedInMemberId }

            ownAssignments.minByOrNull { it.taskAssignment.dueDateTime }?.let {
                onCompletion.add(it)
                counts[it.taskAssignment.id] = ownAssignments.size - 1
            }
            othersAssignments.minByOrNull { it.taskAssignment.dueDateTime }?.let {
                onCompletion.add(it)
                counts[it.taskAssignment.id] = othersAssignments.size - 1
            }
        }

        // 2. Timed assignments bucket classification
        val pastDue = mutableListOf<TaskAssignmentDetails>()
        val dueToday = mutableListOf<TaskAssignmentDetails>()
        val dueTomorrow = mutableListOf<TaskAssignmentDetails>()
        val futureAssignments = mutableListOf<TaskAssignmentDetails>()

        timedAssignments.forEach { taskAssignment ->
            val difference = differenceInDays(taskAssignment.taskAssignment.dueDateTime, now)
            if (difference < 0) {
                pastDue.add(taskAssignment)
            } else if (difference == 0) {
                dueToday.add(taskAssignment)
            } else if (difference == 1) {
                dueTomorrow.add(taskAssignment)
            } else {
                futureAssignments.add(taskAssignment)
            }
        }

        pastDue.sortBy { it.taskAssignment.dueDateTime }
        dueToday.sortBy { it.taskAssignment.dueDateTime }
        dueTomorrow.sortBy { it.taskAssignment.dueDateTime }

        // 3. Future assignments combining (only for future)
        val dueInFuture = mutableListOf<TaskAssignmentDetails>()
        futureAssignments.groupBy { it.taskAssignment.taskId }.forEach { (_, assignmentsForTask) ->
            val (ownAssignments, othersAssignments) = assignmentsForTask
                .partition { it.taskAssignment.memberId == loggedInMemberId }

            ownAssignments.minByOrNull { it.taskAssignment.dueDateTime }?.let {
                dueInFuture.add(it)
                counts[it.taskAssignment.id] = ownAssignments.size - 1
            }
            othersAssignments.minByOrNull { it.taskAssignment.dueDateTime }?.let {
                dueInFuture.add(it)
                counts[it.taskAssignment.id] = othersAssignments.size - 1
            }
        }
        dueInFuture.sortBy { it.taskAssignment.dueDateTime }

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
