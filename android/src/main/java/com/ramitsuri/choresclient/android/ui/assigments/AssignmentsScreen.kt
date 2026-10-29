package com.ramitsuri.choresclient.android.ui.assigments

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.common.AppFAB
import com.ramitsuri.choresclient.android.ui.common.AppTopAppBar
import com.ramitsuri.choresclient.android.ui.common.PersonalSharedToggle
import com.ramitsuri.choresclient.android.ui.common.SyncStatusCard
import com.ramitsuri.choresclient.android.ui.common.TaskCard
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.Assignments
import com.ramitsuri.choresclient.model.view.AssignmentsMenuItem
import com.ramitsuri.choresclient.model.view.AssignmentsViewState
import com.ramitsuri.choresclient.model.view.ListTypeSelection
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.rememberKonfettiState
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun AssignmentsScreen(
    viewState: AssignmentsViewState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onUndoPendingAction: (String) -> Unit = {},
    onSnooze: (String, SnoozeType) -> Unit,
    onTransferAssignmentClicked: (String) -> Unit = {},
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(paddingValues)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal
                        )
                    )
                    .padding(horizontal = MaterialTheme.dimens.large)
            ) {
                PersonalSharedToggle(
                    selectedTypes = viewState.selectedListTypes,
                    onListTypeClicked = onListTypeClicked,
                    modifier = Modifier.align(Alignment.Start)
                )

                if (viewState.unsyncedTaskCount > 0 || viewState.isUploading) {
                    SyncStatusCard(
                        unsyncedTaskCount = viewState.unsyncedTaskCount,
                        unsyncedAddCount = viewState.unsyncedAddCount,
                        unsyncedEditCount = viewState.unsyncedEditCount,
                        isUploading = viewState.isUploading,
                        isSyncScheduled = viewState.isSyncScheduled,
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

                AssignmentsScreenContent(
                    loading = viewState.loading,
                    assignments = viewState.assignments,
                    pendingRemovals = viewState.pendingRemovals,
                    allowSwipeForCompletion = viewState.allowSwipeForCompletion,
                    onMarkAsDone = onMarkAsDoneCelebration,
                    onMarkAsWontDo = onMarkAsWontDo,
                    onUndoPendingAction = onUndoPendingAction,
                    onSnooze = onSnooze,
                    onTransferAssignmentClicked = onTransferAssignmentClicked,
                    onEditTaskClicked = onEditTaskClicked
                )
            }

            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                state = konfetti
            )
        }
    }
}

@Composable
private fun AssignmentsScreenContent(
    loading: Boolean,
    assignments: Assignments,
    pendingRemovals: Map<String, Long>,
    allowSwipeForCompletion: Boolean,
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onUndoPendingAction: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onTransferAssignmentClicked: (String) -> Unit,
    onEditTaskClicked: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (loading) {
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
                    onTransferAssignmentClicked = onTransferAssignmentClicked,
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
    onTransferAssignmentClicked: (String) -> Unit,
    onEditTaskClicked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
    ) {
        assignmentSection(
            headerRes = R.string.assignment_header_on_completion,
            assignments = assignments.onCompletion,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onTransferAssignmentClicked = onTransferAssignmentClicked,
            onEditTaskClicked = onEditTaskClicked,
        )
        assignmentSection(
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
            onTransferAssignmentClicked = onTransferAssignmentClicked,
            onEditTaskClicked = onEditTaskClicked,
        )
        assignmentSection(
            headerRes = R.string.assignment_header_due_today,
            assignments = assignments.dueToday,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onTransferAssignmentClicked = onTransferAssignmentClicked,
            onEditTaskClicked = onEditTaskClicked,
        )
        assignmentSection(
            headerRes = R.string.assignment_header_due_tomorrow,
            assignments = assignments.dueTomorrow,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onTransferAssignmentClicked = onTransferAssignmentClicked,
            onEditTaskClicked = onEditTaskClicked,
        )
        assignmentSection(
            headerRes = R.string.assignment_header_due_in_future,
            assignments = assignments.dueInFuture,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            pendingRemovals = pendingRemovals,
            allowSwipeForCompletion = allowSwipeForCompletion,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onUndoPendingAction = onUndoPendingAction,
            onSnooze = onSnooze,
            onTransferAssignmentClicked = onTransferAssignmentClicked,
            onEditTaskClicked = onEditTaskClicked,
        )
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.assignmentSection(
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
    onTransferAssignmentClicked: (String) -> Unit,
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
                onSnoozeHour = { id -> onSnooze(id, SnoozeType.NextBatch) },
                onTransferClicked = onTransferAssignmentClicked,
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
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = MaterialTheme.dimens.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPastDue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (isPastDue) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPastDue) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.assignment_empty),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
