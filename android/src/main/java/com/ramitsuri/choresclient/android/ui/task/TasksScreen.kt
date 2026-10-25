package com.ramitsuri.choresclient.android.ui.task

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ramitsuri.choresclient.android.ui.common.PillChip
import com.ramitsuri.choresclient.android.ui.common.SyncStatusCard
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatRepeatUnitCompact
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.ListTypeSelection
import com.ramitsuri.choresclient.model.view.TaskDetails
import com.ramitsuri.choresclient.model.view.TasksViewState
import com.ramitsuri.choresclient.utils.now
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock

@Composable
fun TasksScreen(
    viewState: TasksViewState,
    onBack: () -> Unit,
    onAddTaskClicked: () -> Unit,
    onEditTaskClicked: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onListTypeClicked: (ListTypeSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.tasks),
                onBack = onBack
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            AppFAB(onClick = onAddTaskClicked)
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
                .padding(horizontal = MaterialTheme.dimens.large)
        ) {
            OutlinedTextField(
                value = viewState.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text(stringResource(id = R.string.search_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (viewState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(MaterialTheme.dimens.cardCornerRadius),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

            PersonalSharedToggle(
                selectedTypes = viewState.selectedListTypes,
                onListTypeClicked = onListTypeClicked,
            )

            if (viewState.unsyncedTaskCount > 0 || viewState.isUploading) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
                SyncStatusCard(
                    unsyncedTaskCount = viewState.unsyncedTaskCount,
                    isUploading = viewState.isUploading,
                    isSyncScheduled = viewState.isSyncScheduled
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
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
                        TemplateCardItem(
                            details = details,
                            onEditTaskClicked = onEditTaskClicked
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCardItem(
    details: TaskDetails,
    onEditTaskClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val task = details.task
    val roundedCornerShape = RoundedCornerShape(MaterialTheme.dimens.cardCornerRadius)
    Card(
        shape = roundedCornerShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(roundedCornerShape)
            .clickable { onEditTaskClicked(task.id) }
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = task.status)
            }

            val showDescription = task.description.isNotBlank() &&
                    !task.description.trim().equals(task.name.trim(), ignoreCase = true)
            if (showDescription) {
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium),
            ) {
                if (details.houseName.isNotBlank()) {
                    PillChip(
                        text = details.houseName,
                        icon = Icons.Default.House,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (task.repeatUnit != RepeatUnit.NONE && task.repeatValue != 0) {
                    val repeatTextValue = formatRepeatUnitCompact(
                        repeatValue = task.repeatValue,
                        repeatUnit = task.repeatUnit
                    )
                    PillChip(
                        text = repeatTextValue,
                        icon = Icons.Default.Repeat,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (task.rotateMember) {
                    PillChip(
                        text = stringResource(id = R.string.add_task_rotate_member),
                        icon = Icons.Default.Groups,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                if (details.isSyncPending) {
                    PillChip(
                        text = stringResource(id = R.string.sync_status_unsynced_badge),
                        icon = Icons.Default.CloudSync,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = containerColor)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = stringResource(id = statusTextRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
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
                            memberName = "Alex",
                            houseName = "Home"
                        )
                    ),
                    selectedListTypes = setOf(ListTypeSelection.PERSONAL),
                ),
                onBack = {},
                onAddTaskClicked = {},
                onEditTaskClicked = {},
                onSearchQueryChanged = {},
                onListTypeClicked = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewTasksScreenUnsynced() {
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
                                status = ActiveStatus.ACTIVE,
                            ),
                            memberName = "Alex",
                            houseName = "Home",
                            isSyncPending = true
                        )
                    ),
                    selectedListTypes = setOf(ListTypeSelection.PERSONAL),
                    unsyncedTaskCount = 1,
                    isSyncScheduled = true,
                ),
                onBack = {},
                onAddTaskClicked = {},
                onEditTaskClicked = {},
                onSearchQueryChanged = {},
                onListTypeClicked = {},
            )
        }
    }
}
