package com.ramitsuri.choresclient.android.ui.assigments

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.common.AppFAB
import com.ramitsuri.choresclient.android.ui.common.AppTopAppBar
import com.ramitsuri.choresclient.android.ui.common.PersonalSharedToggle
import com.ramitsuri.choresclient.android.ui.common.TaskCard
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.model.entities.RepeatInfo
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.Assignments
import com.ramitsuri.choresclient.model.view.AssignmentsMenuItem
import com.ramitsuri.choresclient.model.view.AssignmentsViewState
import com.ramitsuri.choresclient.model.view.ListTypeSelection
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.utils.now
import kotlinx.datetime.LocalDateTime
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.rememberKonfettiState
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

@Composable
fun AssignmentsScreen(
    viewState: AssignmentsViewState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onUndoPendingAction: (String) -> Unit = {},
    onSnooze: (String, SnoozeType) -> Unit,
    onAddTaskClicked: () -> Unit,
    onEditTaskClicked: (String) -> Unit,
    onSettingsClicked: () -> Unit,
    onCompletedClicked: () -> Unit = {},
    onTasksClicked: () -> Unit = {},
    onListTypeClicked: (ListTypeSelection) -> Unit,
) {
    val menu = listOf(
        AssignmentsMenuItem.COMPLETED,
        AssignmentsMenuItem.TASKS,
        AssignmentsMenuItem.SETTINGS,
    )
    val konfetti = rememberKonfettiState()
    val onMarkAsDoneCelebration: (String) -> Unit = { taskAssignmentId ->
        konfetti.start(
            Party(
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
            )
        )
        onMarkAsDone(taskAssignmentId)
    }
    val onMarkAsWontDoCelebration: (String) -> Unit = { taskAssignmentId ->
        konfetti.start(
            Party(
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
            )
        )
        onMarkAsWontDo(taskAssignmentId)
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.active_todos_title),
                menuItems = menu,
                onMenuItemSelected = { menuItem ->
                    when (menuItem.id) {
                        AssignmentsMenuItem.SETTINGS.id -> onSettingsClicked()
                        AssignmentsMenuItem.COMPLETED.id -> onCompletedClicked()
                        AssignmentsMenuItem.TASKS.id -> onTasksClicked()
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            AppFAB(onClick = onAddTaskClicked)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
        ) {
            AssignmentsContent(
                isLoading = viewState.loading,
                assignments = viewState.assignments,
                pendingRemovals = viewState.pendingRemovals,
                allowSwipeForCompletion = viewState.allowSwipeForCompletion,
                onMarkAsDone = onMarkAsDoneCelebration,
                onMarkAsWontDo = onMarkAsWontDoCelebration,
                onUndoPendingAction = onUndoPendingAction,
                onSnooze = onSnooze,
                onEditTaskClicked = onEditTaskClicked,
                modifier = Modifier
                    .fillMaxSize(),
                selectedListTypes = viewState.selectedListTypes,
                onListTypeClicked = onListTypeClicked,
            )
            KonfettiView(state = konfetti, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun AssignmentsContent(
    isLoading: Boolean,
    assignments: Assignments,
    pendingRemovals: Map<String, Long>,
    allowSwipeForCompletion: Boolean,
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onUndoPendingAction: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onEditTaskClicked: (String) -> Unit,
    selectedListTypes: Set<ListTypeSelection>,
    onListTypeClicked: (ListTypeSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.dimens.large)
    ) {
        PersonalSharedToggle(
            selectedTypes = selectedListTypes,
            onListTypeClicked = onListTypeClicked
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else if (assignments.isEmpty()) {
            EmptyContent()
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AssignmentsList(
                    assignments = assignments,
                    pendingRemovals = pendingRemovals,
                    allowSwipeForCompletion = allowSwipeForCompletion,
                    onMarkAsDone = onMarkAsDone,
                    onMarkAsWontDo = onMarkAsWontDo,
                    onUndoPendingAction = onUndoPendingAction,
                    onSnooze = onSnooze,
                    onEditTaskClicked = onEditTaskClicked,
                )
            }
        }
    }
}

@Composable
private fun AssignmentsList(
    assignments: Assignments,
    pendingRemovals: Map<String, Long>,
    allowSwipeForCompletion: Boolean,
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onUndoPendingAction: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onEditTaskClicked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
    ) {
        assignmentsGroup(
            headerRes = R.string.assignment_header_past_due,
            assignments = assignments.pastDue,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            isPastDue = true,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
        )
        assignmentsGroup(
            headerRes = R.string.assignment_header_due_today,
            assignments = assignments.dueToday,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
        )
        assignmentsGroup(
            headerRes = R.string.assignment_header_due_tomorrow,
            assignments = assignments.dueTomorrow,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
        )
        assignmentsGroup(
            headerRes = R.string.assignment_header_due_in_future,
            assignments = assignments.dueInFuture,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
        )
        assignmentsGroup(
            headerRes = R.string.assignment_header_on_completion,
            assignments = assignments.onCompletion,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
        )
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun LazyListScope.assignmentsGroup(
    @StringRes headerRes: Int,
    assignments: List<TaskAssignmentDetails>,
    otherAssignmentsCount: Map<String, Int>,
    pendingRemovals: Map<String, Long>,
    isPastDue: Boolean = false,
    allowSwipeForCompletion: Boolean,
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onUndoPendingAction: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onEditTaskClicked: (String) -> Unit,
) {
    if (assignments.isNotEmpty()) {
        stickyHeader(key = headerRes) {
            AssignmentHeader(
                title = stringResource(id = headerRes),
                count = assignments.size,
                isPastDue = isPastDue
            )
        }
        items(assignments, key = { it.taskAssignment.id }) { item ->
            val pendingStartTime = pendingRemovals[item.taskAssignment.id]
            TaskCard(
                details = item,
                otherAssignmentsCount = otherAssignmentsCount[item.taskAssignment.id] ?: 0,
                isCompleted = false,
                isPendingRemoval = pendingStartTime != null,
                pendingStartTimeEpochMillis = pendingStartTime ?: 0L,
                isSwipeEnabled = allowSwipeForCompletion,
                onUndoPendingAction = onUndoPendingAction,
                onMarkAsDone = onMarkAsDone,
                onMarkAsWontDo = onMarkAsWontDo,
                onSnoozeHour = { id -> onSnooze(id, SnoozeType.SixHours) },
                onEditTaskClicked = onEditTaskClicked,
            )
        }
    }
}

@Composable
private fun AssignmentHeader(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    isPastDue: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = MaterialTheme.dimens.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPastDue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
        )

        if (count > 1) {
            val badgeContainer = if (isPastDue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
            val badgeContent = if (isPastDue) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeContainer)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeContent
                )
            }
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.assignment_empty),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview
@Composable
private fun PreviewAssignmentContent() {
    val viewState = AssignmentsViewState(
        loading = false,
        assignments = Assignments(
            onCompletion = listOf(),
            pastDue = listOf(
                TaskAssignmentDetails(
                    taskAssignment = TaskAssignment(
                        id = "1",
                        progressStatus = ProgressStatus.TODO,
                        progressStatusDate = Clock.System.now(),
                        taskId = "taskId",
                        taskName = "Pay Rent",
                        houseId = "houseId",
                        repeatInfo = RepeatInfo(
                            repeatValue = 1,
                            repeatUnit = RepeatUnit.MONTH,
                            repeatEndDateTime = null
                        ),
                        memberId = "memberId",
                        memberName = "Alex R.",
                        dueDateTime = LocalDateTime.now()
                    ),
                    reminderTime = null,
                    enableSnooze = true,
                    willReminderBeSet = false,
                    assignedToLoggedInUser = true,
                )
            ),
            dueToday = listOf(
                TaskAssignmentDetails(
                    taskAssignment = TaskAssignment(
                        id = "2",
                        progressStatus = ProgressStatus.TODO,
                        progressStatusDate = Clock.System.now(),
                        taskId = "taskId",
                        taskName = "Water Plants",
                        houseId = "houseId",
                        repeatInfo = RepeatInfo(
                            repeatValue = 3,
                            repeatUnit = RepeatUnit.DAY,
                            repeatEndDateTime = null
                        ),
                        memberId = "memberId",
                        memberName = "You",
                        dueDateTime = LocalDateTime.now()
                    ),
                    reminderTime = null,
                    enableSnooze = true,
                    willReminderBeSet = false,
                    assignedToLoggedInUser = true,
                )
            ),
            dueTomorrow = listOf(),
            dueInFuture = listOf(),
            otherAssignmentsCount = mapOf(),
        ),
        allowSwipeForCompletion = false
    )
    ChoresClientTheme {
        Surface {
            AssignmentsScreen(
                viewState = viewState,
                onMarkAsDone = { },
                onMarkAsWontDo = { },
                onSnooze = { _, _ -> },
                onAddTaskClicked = { },
                onEditTaskClicked = { },
                onSettingsClicked = { },
                onListTypeClicked = { },
            )
        }
    }
}
