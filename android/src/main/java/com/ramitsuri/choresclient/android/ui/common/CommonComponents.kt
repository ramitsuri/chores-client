package com.ramitsuri.choresclient.android.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatReminderAt
import com.ramitsuri.choresclient.android.utils.formatRepeatUnitCompact
import com.ramitsuri.choresclient.model.entities.RepeatInfo
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.AssignmentsMenuItem
import com.ramitsuri.choresclient.model.view.ListTypeSelection
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.utils.getDay
import com.ramitsuri.choresclient.utils.now
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sign
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
    modifier: Modifier = Modifier,
) {
    MultiChoiceSegmentedButtonRow(
        modifier = modifier
            .padding(vertical = MaterialTheme.dimens.small)
    ) {
        ListTypeSelection.entries.forEachIndexed { index, type ->
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
    get() = when (this) {
        ListTypeSelection.PERSONAL -> Icons.Default.Person
        ListTypeSelection.SHARED -> Icons.Default.Groups
    }

private val ListTypeSelection.textResId: Int
    get() = when (this) {
        ListTypeSelection.PERSONAL -> R.string.assignment_filter_personal
        ListTypeSelection.SHARED -> R.string.assignment_filter_shared
    }

/** Finger travel (fraction of card width) needed before the card "unsticks" and the swipe is armed. */
private const val SwipeUnstickFraction = 0.5f

/** Once unstuck, the card re-sticks if the finger travels back below this fraction (hysteresis). */
private const val SwipeRestickFraction = 0.4f

/**
 * Max distance (as a fraction of the unstick distance) the card can visually travel while stuck.
 * Lower = more resistance.
 */
private const val SwipeStuckScaleFraction = 0.6f

/** Releasing while flinging back towards center faster than this (px/s) cancels an armed swipe. */
private const val SwipeCancelVelocity = 1000f

/** Visual distance between haptic ticks while the card is stuck. */
private val SwipeTickStep = 12.dp

/**
 * Swipe-to-act card modeled after the Android 16 notification swipe:
 * - While "stuck", the card follows the finger with progressively increasing resistance and
 *   ticks haptically, ticks spreading out as tension builds.
 * - Once the finger has travelled half the card width the card "unsticks": it springs to the finger,
 *   following 1:1, with a threshold haptic. Dragging back re-sticks it.
 * - Releasing only commits the action when unstuck. Otherwise the card springs back.
 *
 * Swipe right -> [onMarkAsDone], swipe left -> [onMarkAsWontDo].
 */
@Composable
private fun ResistiveSwipeCard(
    onMarkAsDone: () -> Unit,
    onMarkAsWontDo: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val currentOnMarkAsDone by rememberUpdatedState(onMarkAsDone)
    val currentOnMarkAsWontDo by rememberUpdatedState(onMarkAsWontDo)

    val tickStepPx = with(density) { SwipeTickStep.toPx() }
    val iconSizePx = with(density) { MaterialTheme.dimens.iconWidthMedium.toPx() }

    val offset = remember { Animatable(0f) }
    var widthPx by remember { mutableIntStateOf(0) }
    var rawDrag by remember { mutableFloatStateOf(0f) }
    var isUnstuck by remember { mutableStateOf(false) }
    var lastTick by remember { mutableIntStateOf(0) }
    var isDismissing by remember { mutableStateOf(false) }

    val unstickDistance = widthPx * SwipeUnstickFraction
    val restickDistance = widthPx * SwipeRestickFraction
    val stuckScale = unstickDistance * SwipeStuckScaleFraction

    // Rubber band: slope 1 at the start (immediate response), saturating towards stuckScale.
    fun stuckOffset(raw: Float): Float {
        if (stuckScale <= 0f) return 0f
        return sign(raw) * stuckScale * (1f - exp(-abs(raw) / stuckScale))
    }

    fun rawFromStuckOffset(visual: Float): Float {
        if (stuckScale <= 0f) return 0f
        val ratio = (abs(visual) / stuckScale).coerceAtMost(0.99f)
        return sign(visual) * -stuckScale * ln(1f - ratio)
    }

    val followSpec = remember { spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium) }

    val draggableState = rememberDraggableState { delta ->
        if (isDismissing || widthPx == 0) return@rememberDraggableState
        rawDrag += delta
        val distance = abs(rawDrag)
        val wasUnstuck = isUnstuck
        isUnstuck = if (wasUnstuck) distance > restickDistance else distance >= unstickDistance
        if (isUnstuck != wasUnstuck) {
            haptics.performHapticFeedback(
                if (isUnstuck) {
                    HapticFeedbackType.GestureThresholdActivate
                } else {
                    HapticFeedbackType.SegmentTick
                }
            )
        }

        val stuckTarget = stuckOffset(rawDrag)
        val tick = (abs(stuckTarget) / tickStepPx).toInt()
        if (!isUnstuck && !wasUnstuck && tick != lastTick) {
            haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
        lastTick = tick

        val target = if (isUnstuck) rawDrag else stuckTarget
        scope.launch { offset.animateTo(target, followSpec) }
    }

    // Only recompose when the swipe direction flips, not on every frame.
    val direction by remember { derivedStateOf { sign(offset.value) } }
    val isDoneDirection = direction > 0f

    val revealColor by animateColorAsState(
        targetValue = when {
            isDoneDirection && isUnstuck -> MaterialTheme.colorScheme.primary
            isDoneDirection -> MaterialTheme.colorScheme.primaryContainer
            isUnstuck -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.errorContainer
        },
        label = "swipeRevealColor"
    )
    val iconColor by animateColorAsState(
        targetValue = when {
            isDoneDirection && isUnstuck -> MaterialTheme.colorScheme.onPrimary
            isDoneDirection -> MaterialTheme.colorScheme.onPrimaryContainer
            isUnstuck -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onErrorContainer
        },
        label = "swipeIconColor"
    )
    val armedScale by animateFloatAsState(
        targetValue = if (isUnstuck) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipeArmedScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { widthPx = it.width }
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStarted = {
                    if (!isDismissing) {
                        // Pick up from wherever the card currently is (e.g. mid spring-back).
                        rawDrag = rawFromStuckOffset(offset.value)
                        lastTick = (abs(offset.value) / tickStepPx).toInt()
                    }
                },
                onDragStopped = { velocity ->
                    if (isDismissing) return@draggable
                    val swipeDirection = sign(offset.value)
                    val isFlingingBack = velocity * swipeDirection < -SwipeCancelVelocity
                    if (isUnstuck && !isFlingingBack && swipeDirection != 0f) {
                        isDismissing = true
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        scope.launch {
                            offset.animateTo(
                                targetValue = swipeDirection * widthPx,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                initialVelocity = velocity
                            )
                            if (swipeDirection > 0f) currentOnMarkAsDone() else currentOnMarkAsWontDo()
                        }
                    } else {
                        if (isUnstuck) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        }
                        isUnstuck = false
                        rawDrag = 0f
                        scope.launch {
                            offset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                initialVelocity = velocity
                            )
                        }
                    }
                }
            )
    ) {
        // Reveal area: grows with the swipe from the very first pixel.
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = if (isDoneDirection) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            if (direction != 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .layout { measurable, constraints ->
                            val revealWidth = abs(offset.value).roundToInt()
                                .coerceIn(0, constraints.maxWidth)
                            val placeable = measurable.measure(
                                constraints.copy(minWidth = revealWidth, maxWidth = revealWidth)
                            )
                            layout(revealWidth, placeable.height) { placeable.place(0, 0) }
                        }
                        .background(revealColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDoneDirection) Icons.Default.Check else Icons.Default.Clear,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .requiredSize(MaterialTheme.dimens.iconWidthMedium)
                            .graphicsLayer {
                                // Scale in with the revealed width so the icon is visible immediately.
                                val growth = (abs(offset.value) / (iconSizePx * 2f)).coerceIn(0f, 1f)
                                val scale = growth * armedScale
                                scaleX = scale
                                scaleY = scale
                                alpha = growth
                            }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = offset.value }
        ) {
            content()
        }
    }
}

@Composable
fun TaskCard(
    details: TaskAssignmentDetails,
    modifier: Modifier = Modifier,
    otherAssignmentsCount: Int = 0,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    isCompleted: Boolean = false,
    isPendingRemoval: Boolean = false,
    pendingStartTimeEpochMillis: Long = 0L,
    isSwipeEnabled: Boolean = true,
    onUndoPendingAction: ((String) -> Unit)? = null,
    onMarkAsDone: ((String) -> Unit)? = null,
    onMarkAsWontDo: ((String) -> Unit)? = null,
    onSnoozeHour: ((String) -> Unit)? = null,
    onEditTaskClicked: ((String) -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }
    var cardHeight by remember { mutableStateOf<Dp?>(null) }
    val density = LocalDensity.current

    var timerProgress by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(isPendingRemoval, pendingStartTimeEpochMillis) {
        if (isPendingRemoval) {
            val durationMillis = 3000L
            val startTime = if (pendingStartTimeEpochMillis > 0L) {
                pendingStartTimeEpochMillis
            } else {
                Clock.System.now().toEpochMilliseconds()
            }
            while (true) {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val elapsed = currentTime - startTime
                if (elapsed >= durationMillis) {
                    timerProgress = 0f
                    break
                }
                timerProgress = (1f - (elapsed.toFloat() / durationMillis)).coerceIn(0f, 1f)
                delay(50.milliseconds)
            }
        } else {
            timerProgress = 1f
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

    val state = if (isPendingRemoval) {
        TaskCardState.PENDING_REMOVAL
    } else if (!isCompleted && onMarkAsDone != null && onMarkAsWontDo != null) {
        if (isSwipeEnabled) {
            TaskCardState.SWIPEABLE
        } else {
            TaskCardState.SWIPE_DISABLED
        }
    } else {
        TaskCardState.NOT_SWIPEABLE
    }
    AnimatedContent(state) { taskCardState ->
        when (taskCardState) {
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
                                text = details.taskAssignment.taskName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = {
                                    onUndoPendingAction?.invoke(details.taskAssignment.id)
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
                        onMarkAsDone?.invoke(details.taskAssignment.id)
                    },
                    onMarkAsWontDo = {
                        onMarkAsWontDo?.invoke(details.taskAssignment.id)
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
                        shape = roundedCornerShape,
                        borderModifier = borderModifier,
                    )
                }
            }

            TaskCardState.SWIPE_DISABLED -> {
                SwipeDisabledTaskCard(
                    details = details,
                    otherAssignmentsCount = otherAssignmentsCount,
                    isCompleted = isCompleted,
                    showMenu = showMenu,
                    timeZone = timeZone,
                    onShowMenuChange = { showMenu = it },
                    onEditTaskClicked = onEditTaskClicked,
                    onSnoozeHour = onSnoozeHour,
                    onMarkAsDone = onMarkAsDone,
                    onMarkAsWontDo = onMarkAsWontDo,
                    shape = roundedCornerShape,
                    borderModifier = borderModifier,
                )
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
                        shape = roundedCornerShape,
                        borderModifier = borderModifier,
                    )
                }

            }
        }
    }
}

private enum class TaskCardState {
    PENDING_REMOVAL,
    SWIPEABLE,
    SWIPE_DISABLED, // Card is swipeable/actionable but not via swipe
    NOT_SWIPEABLE, // Card is not swipeable
}

@Composable
private fun SwipeDisabledTaskCard(
    details: TaskAssignmentDetails,
    otherAssignmentsCount: Int,
    isCompleted: Boolean,
    showMenu: Boolean,
    timeZone: TimeZone,
    onShowMenuChange: (Boolean) -> Unit,
    onEditTaskClicked: ((String) -> Unit)?,
    onSnoozeHour: ((String) -> Unit)?,
    onMarkAsDone: ((String) -> Unit)?,
    onMarkAsWontDo: ((String) -> Unit)?,
    borderModifier: Modifier,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(MaterialTheme.dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.then(borderModifier),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = { onMarkAsDone?.invoke(details.taskAssignment.id) },
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.assignment_mark_completed),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TaskCardContent(
                details = details,
                otherAssignmentsCount = otherAssignmentsCount,
                isCompleted = isCompleted,
                showMenu = showMenu,
                timeZone = timeZone,
                onShowMenuChange = onShowMenuChange,
                onEditTaskClicked = onEditTaskClicked,
                onSnoozeHour = onSnoozeHour,
                onMarkAsWontDo = onMarkAsWontDo,
                borderModifier = Modifier,
                shape = shape,
                addStartPaddingToContent = false
            )
        }
    }
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
    addStartPaddingToContent: Boolean = true,
    borderModifier: Modifier,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val isDone = details.taskAssignment.progressStatus == ProgressStatus.DONE
    val isRecurring = details.taskAssignment.repeatInfo.repeatUnit != RepeatUnit.NONE &&
            details.taskAssignment.repeatInfo.repeatValue != 0

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(borderModifier)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.dimens.large)
                .padding(
                    start = if (addStartPaddingToContent) MaterialTheme.dimens.large else 0.dp,
                    end = MaterialTheme.dimens.large
                )
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

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small),
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
    modifier: Modifier = Modifier,
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
    text: String = stringResource(id = R.string.assignment_add_task_content_description),
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

@Preview
@Composable
private fun Preview() {
    ChoresClientTheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TaskCard(
                    details = TaskAssignmentDetails(
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
                    ),
                    otherAssignmentsCount = 2,
                    isCompleted = false,
                    timeZone = TimeZone.currentSystemDefault(),
                    onEditTaskClicked = { },
                    onSnoozeHour = { },
                    onMarkAsDone = { },
                    onMarkAsWontDo = { },
                )
                TaskCard(
                    details = TaskAssignmentDetails(
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
                    ),
                    otherAssignmentsCount = 2,
                    isCompleted = false,
                    isSwipeEnabled = false,
                    timeZone = TimeZone.currentSystemDefault(),
                    onEditTaskClicked = { },
                    onSnoozeHour = { },
                    onMarkAsDone = { },
                    onMarkAsWontDo = { },
                )
            }
        }
    }
}
