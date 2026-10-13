package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.Assignments
import com.ramitsuri.choresclient.model.view.AssignmentsViewState
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.differenceInDays
import com.ramitsuri.choresclient.utils.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock

class AssignmentsViewModel(
    assignmentId: String?,
    private val repository: TaskAssignmentsRepository,
    private val prefManager: PrefManager,
    private val longLivingCoroutineScope: CoroutineScope
) : ViewModel() {

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

    private fun getAssignments(): Flow<AssignmentsViewState> {
        val loggedInMemberId = prefManager.getLoggedInMemberId()
        return if (loggedInMemberId == null) {
            flowOf(AssignmentsViewState(loading = false))
        } else {
            repository.getLocalFlow(loggedInMemberId).map { taskAssignmentDetails ->
                val filtered = taskAssignmentDetails
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
                    assignments = assignments
                )
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