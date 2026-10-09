package com.ramitsuri.choresclient.android.ui.task

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.assigments.FilterRow
import com.ramitsuri.choresclient.android.ui.assigments.LabelWithIcon
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatRepeatUnitCompact
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem
import com.ramitsuri.choresclient.model.view.TaskDetails
import com.ramitsuri.choresclient.model.view.TasksViewState
import com.ramitsuri.choresclient.utils.now
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime

@Composable
fun TasksScreen(
    viewState: TasksViewState,
    onBack: () -> Unit,
    onAddTaskClicked: () -> Unit,
    onEditTaskClicked: (String) -> Unit,
    onFilterItemClicked: (Filter, FilterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClicked) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.add_task_add)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal
                    )
                )
                .padding(horizontal = MaterialTheme.dimens.medium)
        ) {
            Spacer(
                modifier = Modifier.height(
                    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.dimens.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
            if (viewState.filters.isNotEmpty()) {
                FilterRow(
                    filters = viewState.filters,
                    onFilterSelected = onFilterItemClicked,
                    menu = emptyList(),
                    onMenuSelected = {}
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
            }
            if (viewState.loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } else if (viewState.tasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.tasks_empty),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
                ) {
                    items(
                        items = viewState.tasks,
                        key = { it.task.id }
                    ) { details ->
                        TaskItem(
                            details = details,
                            onEditTaskClicked = onEditTaskClicked
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(MaterialTheme.dimens.extraLarge))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    details: TaskDetails,
    onEditTaskClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val task = details.task
    Card(
        onClick = { onEditTaskClicked(task.id) },
        border = BorderStroke(
            0.1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = MaterialTheme.dimens.minAssignmentItemHeight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = MaterialTheme.dimens.small,
                            vertical = MaterialTheme.dimens.medium
                        )
                )
                StatusBadge(status = task.status)
            }
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = MaterialTheme.dimens.medium)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.medium),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (details.memberName.isNotBlank()) {
                    LabelWithIcon(text = details.memberName, icon = Icons.Filled.Person)
                }
                if (details.houseName.isNotBlank()) {
                    if (details.memberName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
                    }
                    LabelWithIcon(text = details.houseName, icon = Icons.Filled.House)
                }
                if (task.repeatUnit != RepeatUnit.NONE && task.repeatValue != 0) {
                    Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
                    val repeatTextValue = formatRepeatUnitCompact(
                        repeatValue = task.repeatValue,
                        repeatUnit = task.repeatUnit
                    )
                    LabelWithIcon(text = repeatTextValue, icon = Icons.Filled.Repeat)
                }
                if (task.rotateMember) {
                    Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
                    LabelWithIcon(
                        text = stringResource(id = R.string.add_task_rotate_member),
                        icon = Icons.Filled.GroupWork
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.dimens.medium),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = { onEditTaskClicked(task.id) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.edit),
                        modifier = Modifier.size(MaterialTheme.dimens.iconWidthSmall)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
                    Text(text = stringResource(id = R.string.edit))
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
        }
    }
}

@Composable
private fun StatusBadge(
    status: ActiveStatus,
    modifier: Modifier = Modifier
) {
    val (statusTextRes, containerColor, contentColor) = when (status) {
        ActiveStatus.ACTIVE -> Triple(
            R.string.edit_task_active_status_active,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )

        ActiveStatus.PAUSED -> Triple(
            R.string.edit_task_active_status_pause,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )

        ActiveStatus.INACTIVE, ActiveStatus.UNKNOWN -> Triple(
            R.string.edit_task_active_status_inactive,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            )
            .padding(MaterialTheme.dimens.small)
    ) {
        Text(
            text = stringResource(id = statusTextRes),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

@Preview
@Composable
private fun PreviewTasksScreen() {
    ChoresClientTheme {
        Surface {
            TasksScreen(
                viewState = TasksViewState(
                    loading = false,
                    tasks = listOf(
                        TaskDetails(
                            task = Task(
                                id = "1",
                                name = "Clean Kitchen",
                                description = "Clean counters and sink",
                                dueDateTime = LocalDateTime.now(),
                                repeatValue = 1,
                                repeatUnit = RepeatUnit.DAY,
                                repeatEndDateTime = null,
                                houseId = "house1",
                                memberId = "member1",
                                rotateMember = true,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            memberName = "Ramit",
                            houseName = "Home"
                        )
                    )
                ),
                onBack = {},
                onAddTaskClicked = {},
                onEditTaskClicked = {},
                onFilterItemClicked = { _, _ -> }
            )
        }
    }
}
