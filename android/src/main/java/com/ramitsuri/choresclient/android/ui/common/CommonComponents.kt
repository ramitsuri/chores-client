package com.ramitsuri.choresclient.android.ui.common

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatReminderAt
import com.ramitsuri.choresclient.android.utils.formatRepeatUnitCompact
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.AssignmentsMenuItem
import com.ramitsuri.choresclient.model.view.ListTypeSelection
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.utils.getDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

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
    selectedTypes: Set<ListTypeSelection>,
    onListTypeClicked: (ListTypeSelection) -> Unit,
    modifier: Modifier = Modifier
) {
    MultiChoiceSegmentedButtonRow(
        modifier = modifier
            .padding(vertical = MaterialTheme.dimens.small)
    ) {
        ListTypeSelection.entries.forEachIndexed { index,  type ->
            SegmentedButton(
                checked = type in selectedTypes,
                onCheckedChange = { onListTypeClicked(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ListTypeSelection.entries.size),
                icon = {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            ) {
                Text(
                    text = stringResource(id = type.textResId),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private val ListTypeSelection.icon: ImageVector
    get() = when(this) {
        ListTypeSelection.PERSONAL -> Icons.Default.Person
        ListTypeSelection.SHARED -> Icons.Default.Groups
    }

private val ListTypeSelection.textResId: Int
    get() = when(this) {
        ListTypeSelection.PERSONAL -> R.string.assignment_filter_personal
        ListTypeSelection.SHARED -> R.string.assignment_filter_shared
    }

enum class DragValue {
    Normal,
    DismissedStart,
    DismissedEnd
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ResistiveSwipeCard(
    onMarkAsDone: () -> Unit,
    onMarkAsWontDo: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val maxSwipeDistance = with(density) { 260.dp.toPx() }
    val scope = rememberCoroutineScope()

    val draggableState = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Normal,
            anchors = DraggableAnchors {
                DragValue.Normal at 0f
                DragValue.DismissedStart at -maxSwipeDistance
                DragValue.DismissedEnd at maxSwipeDistance
            },
            positionalThreshold = { distance: Float -> distance * 0.8f },
            velocityThreshold = { with(density) { 150.dp.toPx() } },
            snapAnimationSpec = tween(durationMillis = 300),
            decayAnimationSpec = exponentialDecay()
        )
    }

    LaunchedEffect(draggableState.currentValue) {
        when (draggableState.currentValue) {
            DragValue.DismissedStart -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onMarkAsDone()
                scope.launch {
                    draggableState.animateTo(DragValue.Normal)
                }
            }

            DragValue.DismissedEnd -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onMarkAsWontDo()
                scope.launch {
                    draggableState.animateTo(DragValue.Normal)
                }
            }

            DragValue.Normal -> {}
        }
    }

    val currentOffset = try {
        draggableState.requireOffset()
    } catch (_: Exception) {
        0f
    }

    val finalOffset = if (currentOffset.isNaN()) 0f else {
        if (currentOffset > maxSwipeDistance) {
            maxSwipeDistance + (currentOffset - maxSwipeDistance) * 0.3f
        } else if (currentOffset < -maxSwipeDistance) {
            -maxSwipeDistance + (currentOffset + maxSwipeDistance) * 0.3f
        } else {
            currentOffset
        }
    }

    val backgroundContainerColor = when {
        finalOffset > 20f -> MaterialTheme.colorScheme.primaryContainer
        finalOffset < -20f -> MaterialTheme.colorScheme.errorContainer
        else -> Color.Transparent
    }
    val backgroundAlignment = if (finalOffset > 0f) Alignment.CenterStart else Alignment.CenterEnd
    val backgroundIcon = if (finalOffset > 0f) Icons.Default.Check else Icons.Default.Clear

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.dimens.cardCornerRadius))
            .background(backgroundContainerColor),
        contentAlignment = backgroundAlignment
    ) {
        if (backgroundContainerColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = MaterialTheme.dimens.large),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = backgroundIcon,
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.dimens.iconWidthMedium)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = finalOffset.roundToInt(), y = 0) }
                .anchoredDraggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal
                )
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    details: TaskAssignmentDetails,
    modifier: Modifier = Modifier,
    otherAssignmentsCount: Int = 0,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    isCompleted: Boolean = false,
    onMarkAsDone: ((String) -> Unit)? = null,
    onMarkAsWontDo: ((String) -> Unit)? = null,
    onSnoozeHour: ((String) -> Unit)? = null,
    onEditTaskClicked: ((String) -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }
    var isPendingRemoval by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var cardHeight by remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current

    var timerProgress by remember { mutableStateOf(1f) }

    LaunchedEffect(isPendingRemoval) {
        if (isPendingRemoval) {
            timerProgress = 1f
            val durationMillis = 5000L
            val startTime = Clock.System.now().toEpochMilliseconds()
            while (true) {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val elapsed = currentTime - startTime
                if (elapsed >= durationMillis) {
                    break
                }
                timerProgress = (1f - (elapsed.toFloat() / durationMillis)).coerceIn(0f, 1f)
                delay(50.milliseconds)
            }
            timerProgress = 0f
            pendingAction?.invoke()
            isPendingRemoval = false
        }
    }

    val containerModifier = modifier
        .fillMaxWidth()
        .then(
            if (cardHeight != null) {
                Modifier.height(cardHeight!!)
            } else {
                Modifier.onGloballyPositioned { coordinates ->
                    cardHeight = with(density) { coordinates.size.height.toDp() }
                }
            }
        )

    val state = if (isPendingRemoval) {
        TaskCardState.PENDING_REMOVAL
    } else if (!isCompleted && onMarkAsDone != null && onMarkAsWontDo != null) {
        TaskCardState.SWIPEABLE
    } else {
        TaskCardState.NOT_SWIPEABLE
    }
    AnimatedContent(state) { taskCardState ->
        when(taskCardState ){
            TaskCardState.PENDING_REMOVAL -> {
                Card(
                    shape = RoundedCornerShape(MaterialTheme.dimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = containerModifier
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(MaterialTheme.dimens.large),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.completed),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = {
                                    isPendingRemoval = false
                                    pendingAction = null
                                }
                            ) {
                                Text(
                                    text = stringResource(id = R.string.undo),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { timerProgress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            TaskCardState.SWIPEABLE -> {
                ResistiveSwipeCard(
                    onMarkAsDone = {
                        pendingAction = { onMarkAsDone?.invoke(details.taskAssignment.id) }
                        isPendingRemoval = true
                    },
                    onMarkAsWontDo = {
                        pendingAction = { onMarkAsWontDo?.invoke(details.taskAssignment.id) }
                        isPendingRemoval = true
                    },
                    modifier = containerModifier
                ) {
                    TaskCardContent(
                        details = details,
                        otherAssignmentsCount = otherAssignmentsCount,
                        isCompleted = isCompleted,
                        showMenu = showMenu,
                        timeZone = timeZone,
                        onShowMenuChange = { showMenu = it },
                        onEditTaskClicked = onEditTaskClicked,
                        onSnoozeHour = onSnoozeHour,
                        onMarkAsWontDo = onMarkAsWontDo,
                    )
                }
            }
            TaskCardState.NOT_SWIPEABLE -> {
                Box(modifier = containerModifier) {
                    TaskCardContent(
                        details = details,
                        otherAssignmentsCount = otherAssignmentsCount,
                        isCompleted = isCompleted,
                        showMenu = showMenu,
                        timeZone = timeZone,
                        onShowMenuChange = { showMenu = it },
                        onEditTaskClicked = onEditTaskClicked,
                        onSnoozeHour = onSnoozeHour,
                        onMarkAsWontDo = onMarkAsWontDo,
                    )
                }

            }
        }
    }
}

private enum class TaskCardState {
    PENDING_REMOVAL,
    SWIPEABLE,
    NOT_SWIPEABLE,
}

@Composable
private fun TaskCardContent(
    details: TaskAssignmentDetails,
    otherAssignmentsCount: Int,
    isCompleted: Boolean,
    showMenu: Boolean,
    timeZone: TimeZone,
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
                    val completedText = getDay(
                        toFormat = details.taskAssignment.progressStatusDate.toLocalDateTime(timeZone),
                        simplifyToday = true,
                        simplifyTomorrow = true
                    ).string()
                    Text(
                        text = stringResource(
                            id = if (isDone) R.string.completed_today else R.string.wont_do,
                            completedText,
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
