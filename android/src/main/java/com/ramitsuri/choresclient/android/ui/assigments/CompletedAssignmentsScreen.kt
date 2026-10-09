package com.ramitsuri.choresclient.android.ui.assigments

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatRepeatUnitCompact
import com.ramitsuri.choresclient.model.entities.RepeatInfo
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem
import com.ramitsuri.choresclient.model.view.CompletedAssignmentsViewState
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.utils.getDay
import com.ramitsuri.choresclient.utils.now
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock

@Composable
fun CompletedAssignmentsScreen(
    viewState: CompletedAssignmentsViewState,
    onBack: () -> Unit,
    onFilterItemClicked: (Filter, FilterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
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
            } else if (viewState.assignments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.completed_empty),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
                ) {
                    items(
                        items = viewState.assignments,
                        key = { it.taskAssignment.id }
                    ) { details ->
                        CompletedAssignmentItem(details = details)
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
private fun CompletedAssignmentItem(
    details: TaskAssignmentDetails,
    modifier: Modifier = Modifier
) {
    Card(
        border = if (details.assignedToLoggedInUser) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else {
            BorderStroke(
                0.1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
            )
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row {
            Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
            val isDone = details.taskAssignment.progressStatus == ProgressStatus.DONE
            val containerColor = if (isDone) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
            val contentColor = if (isDone) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            }
            FilledTonalIconButton(
                onClick = {},
                enabled = false,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    disabledContainerColor = containerColor,
                    disabledContentColor = contentColor
                ),
                modifier = Modifier
                    .width(MaterialTheme.dimens.iconWidthLarge)
                    .align(alignment = Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Default.Check else Icons.Default.Clear,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .defaultMinSize(minHeight = MaterialTheme.dimens.minAssignmentItemHeight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.medium),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = details.taskAssignment.taskName,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.dimens.small,
                            vertical = MaterialTheme.dimens.medium
                        )
                    )
                    if (details.taskAssignment.repeatInfo.repeatUnit != RepeatUnit.ON_COMPLETE) {
                        Text(
                            text = getDay(
                                toFormat = details.taskAssignment.dueDateTime,
                                simplifyToday = false,
                                simplifyTomorrow = false
                            ).string(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(
                                        bottomStart = 8.dp,
                                        bottomEnd = 8.dp
                                    )
                                )
                                .padding(MaterialTheme.dimens.small)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.dimens.medium),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val text = details.taskAssignment.memberName
                    LabelWithIcon(text = text, icon = Icons.Filled.Person)
                    if (details.taskAssignment.repeatInfo.repeatUnit != RepeatUnit.NONE &&
                        details.taskAssignment.repeatInfo.repeatValue != 0
                    ) {
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
                        val repeatTextValue = formatRepeatUnitCompact(
                            repeatValue = details.taskAssignment.repeatInfo.repeatValue,
                            repeatUnit = details.taskAssignment.repeatInfo.repeatUnit
                        )
                        LabelWithIcon(text = repeatTextValue, icon = Icons.Filled.Repeat)
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCompletedAssignmentsScreen() {
    ChoresClientTheme {
        Surface {
            CompletedAssignmentsScreen(
                viewState = CompletedAssignmentsViewState(
                    loading = false,
                    assignments = listOf(
                        TaskAssignmentDetails(
                            taskAssignment = TaskAssignment(
                                id = "1",
                                progressStatus = ProgressStatus.DONE,
                                progressStatusDate = Clock.System.now(),
                                taskId = "taskId",
                                taskName = "Task 1",
                                houseId = "houseId",
                                repeatInfo = RepeatInfo(
                                    repeatValue = 1,
                                    repeatUnit = RepeatUnit.DAY,
                                    repeatEndDateTime = null
                                ),
                                memberId = "memberId",
                                memberName = "Member",
                                dueDateTime = LocalDateTime.now()
                            ),
                            reminderTime = null,
                            enableSnooze = false,
                            willReminderBeSet = false,
                            assignedToLoggedInUser = false,
                        ),
                        TaskAssignmentDetails(
                            taskAssignment = TaskAssignment(
                                id = "2",
                                progressStatus = ProgressStatus.WONT_DO,
                                progressStatusDate = Clock.System.now(),
                                taskId = "taskId",
                                taskName = "Task 2",
                                houseId = "houseId",
                                repeatInfo = RepeatInfo(
                                    repeatValue = 1,
                                    repeatUnit = RepeatUnit.DAY,
                                    repeatEndDateTime = null
                                ),
                                memberId = "memberId",
                                memberName = "Member",
                                dueDateTime = LocalDateTime.now()
                            ),
                            reminderTime = null,
                            enableSnooze = false,
                            willReminderBeSet = false,
                            assignedToLoggedInUser = true,
                        )
                    ),
                    filters = listOf()
                ),
                onBack = {},
                onFilterItemClicked = { _, _ -> }
            )
        }
    }
}
