package com.ramitsuri.choresclient.android.ui.common

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatReminderAt
import com.ramitsuri.choresclient.android.utils.formatRepeatUnitCompact
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.AssignmentsMenuItem
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.utils.getDay

enum class ListTypeSelection {
    PERSONAL,
    SHARED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    menuItems: List<AssignmentsMenuItem> = emptyList(),
    onMenuItemSelected: ((AssignmentsMenuItem) -> Unit)? = null,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
        },
        actions = {
            if (menuItems.isNotEmpty() && onMenuItemSelected != null) {
                Box {
                    IconButton(onClick = { menuExpanded = !menuExpanded }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.assignment_menu_content_description)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        menuItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item.text.string()) },
                                onClick = {
                                    menuExpanded = false
                                    onMenuItemSelected(item)
                                }
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalSharedToggle(
    selectedType: ListTypeSelection,
    onTypeSelected: (ListTypeSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.dimens.small)
    ) {
        SegmentedButton(
            selected = selectedType == ListTypeSelection.PERSONAL,
            onClick = { onTypeSelected(ListTypeSelection.PERSONAL) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        ) {
            Text(
                text = stringResource(id = R.string.assignment_filter_personal),
                style = MaterialTheme.typography.labelLarge
            )
        }
        SegmentedButton(
            selected = selectedType == ListTypeSelection.SHARED,
            onClick = { onTypeSelected(ListTypeSelection.SHARED) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        ) {
            Text(
                text = stringResource(id = R.string.assignment_filter_shared),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    details: TaskAssignmentDetails,
    modifier: Modifier = Modifier,
    otherAssignmentsCount: Int = 0,
    isCompleted: Boolean = false,
    onMarkAsDone: ((String) -> Unit)? = null,
    onMarkAsWontDo: ((String) -> Unit)? = null,
    onSnoozeHour: ((String) -> Unit)? = null,
    onEditTaskClicked: ((String) -> Unit)? = null,
) {
    val view = LocalView.current
    var showMenu by remember { mutableStateOf(false) }

    if (!isCompleted && onMarkAsDone != null && onMarkAsWontDo != null) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onMarkAsDone(details.taskAssignment.id)
                        true
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onMarkAsWontDo(details.taskAssignment.id)
                        true
                    }

                    SwipeToDismissBoxValue.Settled -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                val color = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                }
                val alignment = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    SwipeToDismissBoxValue.Settled -> Alignment.CenterStart
                }
                val icon = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Clear
                    SwipeToDismissBoxValue.Settled -> Icons.Default.Check
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MaterialTheme.dimens.cardCornerRadius))
                        .background(color)
                        .padding(horizontal = MaterialTheme.dimens.large),
                    contentAlignment = alignment
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.dimens.iconWidthMedium)
                    )
                }
            },
            modifier = modifier
        ) {
            TaskCardContent(
                details = details,
                otherAssignmentsCount = otherAssignmentsCount,
                isCompleted = isCompleted,
                showMenu = showMenu,
                onShowMenuChange = { showMenu = it },
                onEditTaskClicked = onEditTaskClicked,
                onSnoozeHour = onSnoozeHour,
                onMarkAsWontDo = onMarkAsWontDo,
            )
        }
    } else {
        TaskCardContent(
            details = details,
            otherAssignmentsCount = otherAssignmentsCount,
            isCompleted = isCompleted,
            showMenu = showMenu,
            onShowMenuChange = { showMenu = it },
            onEditTaskClicked = onEditTaskClicked,
            onSnoozeHour = onSnoozeHour,
            onMarkAsWontDo = onMarkAsWontDo,
            modifier = modifier
        )
    }
}

@Composable
private fun TaskCardContent(
    details: TaskAssignmentDetails,
    otherAssignmentsCount: Int,
    isCompleted: Boolean,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    onEditTaskClicked: ((String) -> Unit)?,
    onSnoozeHour: ((String) -> Unit)?,
    onMarkAsWontDo: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isDone = details.taskAssignment.progressStatus == ProgressStatus.DONE
    val isRecurring = details.taskAssignment.repeatInfo.repeatUnit != RepeatUnit.NONE &&
            details.taskAssignment.repeatInfo.repeatValue != 0

    val roundedCornerShape = RoundedCornerShape(MaterialTheme.dimens.cardCornerRadius)
    val borderModifier = if (details.assignedToLoggedInUser) {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            shape = roundedCornerShape,
        )
    } else {
        Modifier
    }
    Card(
        shape = roundedCornerShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(roundedCornerShape)
            .then(borderModifier)
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
                    text = details.taskAssignment.taskName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (isCompleted && isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (!isCompleted && (onEditTaskClicked != null || onSnoozeHour != null || onMarkAsWontDo != null)) {
                    Box {
                        IconButton(
                            onClick = { onShowMenuChange(true) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(id = R.string.assignment_menu_content_description),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { onShowMenuChange(false) }
                        ) {
                            if (onSnoozeHour != null && details.enableSnooze) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.assignment_details_button_snooze_six_hours)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Snooze,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        onShowMenuChange(false)
                                        onSnoozeHour(details.taskAssignment.id)
                                    }
                                )
                            }
                            if (onEditTaskClicked != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.edit)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        onShowMenuChange(false)
                                        onEditTaskClicked(details.taskAssignment.taskId)
                                    }
                                )
                            }
                            if (onMarkAsWontDo != null) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = R.string.assignment_details_button_wont_do)) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        onShowMenuChange(false)
                                        onMarkAsWontDo(details.taskAssignment.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cadence Pill Tag (Only if recurring)
                if (isRecurring) {
                    val repeatText = formatRepeatUnitCompact(
                        repeatValue = details.taskAssignment.repeatInfo.repeatValue,
                        repeatUnit = details.taskAssignment.repeatInfo.repeatUnit
                    )
                    PillChip(
                        text = repeatText,
                        icon = Icons.Default.Repeat,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                // Assignee Tag
                PillChip(
                    text = details.taskAssignment.memberName,
                    icon = Icons.Default.Person,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (otherAssignmentsCount > 0) {
                    PillChip(
                        text = pluralStringResource(
                            id = R.plurals.assignment_iterations_more,
                            count = otherAssignmentsCount,
                            otherAssignmentsCount
                        ),
                        icon = Icons.Default.Groups,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Due Date / Time or Completion Status Date
                if (isCompleted) {
                    Text(
                        text = stringResource(
                            id = if (isDone) R.string.completed_today else R.string.wont_do
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (details.taskAssignment.repeatInfo.repeatUnit != RepeatUnit.ON_COMPLETE) {
                    val dueDayText = getDay(
                        toFormat = details.taskAssignment.dueDateTime,
                        simplifyToday = true,
                        simplifyTomorrow = true
                    ).string()
                    Text(
                        text = dueDayText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Reminder Pill
                if (details.willReminderBeSet && details.reminderTime != null) {
                    PillChip(
                        text = formatReminderAt(toFormat = details.reminderTime),
                        icon = Icons.Default.Alarm,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun PillChip(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AppFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = stringResource(id = R.string.assignment_add_task_content_description)
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(MaterialTheme.dimens.fabCornerRadius),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        },
        modifier = modifier
    )
}
