package com.ramitsuri.choresclient.android.ui.assigments

import android.view.HapticFeedbackConstants
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
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
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.filter.Filter
import com.ramitsuri.choresclient.model.filter.FilterItem
import com.ramitsuri.choresclient.model.filter.FilterType
import com.ramitsuri.choresclient.model.filter.PersonFilter
import com.ramitsuri.choresclient.model.filter.PersonFilterItem
import com.ramitsuri.choresclient.model.view.Assignments
import com.ramitsuri.choresclient.model.view.AssignmentsMenuItem
import com.ramitsuri.choresclient.model.view.AssignmentsViewState
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.model.view.TextValue
import com.ramitsuri.choresclient.utils.getDay
import com.ramitsuri.choresclient.utils.now
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime

@Composable
fun AssignmentsScreen(
    viewState: AssignmentsViewState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onFilterItemClicked: (Filter, FilterItem) -> Unit,
    onAddTaskClicked: () -> Unit,
    onEditTaskClicked: (String) -> Unit,
    onSettingsClicked: () -> Unit,
    onCustomHoursEntered: (String) -> Unit,
    onCustomMinutesEntered: (String) -> Unit,
    onCustomTimeSet: (String) -> Unit,
    onCustomTimeCanceled: () -> Unit,
    onItemClicked: (String) -> Unit,
) {
    val menu = listOf(AssignmentsMenuItem.SETTINGS)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClicked,
            ) {
                Icon(
                    Icons.Filled.Add,
                    stringResource(id = R.string.assignment_add_task_content_description)
                )
            }
        }
    ) { paddingValues ->
        AssignmentsContent(
            isLoading = viewState.loading,
            assignments = viewState.assignments,
            expandedAssignmentId = viewState.expandedAssignmentId,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal,
                    ),
                ),
            filters = viewState.filters,
            onFilterSelected = onFilterItemClicked,
            menu = menu,
            customSnoozeHours = viewState.customSnoozeHours,
            customSnoozeMinutes = viewState.customSnoozeMinutes,
            onCustomHoursEntered = onCustomHoursEntered,
            onCustomMinutesEntered = onCustomMinutesEntered,
            onCustomTimeSet = onCustomTimeSet,
            onCustomTimeCanceled = onCustomTimeCanceled,
            onMenuSelected = { menuItem ->
                if (menuItem.id == AssignmentsMenuItem.SETTINGS.id) {
                    onSettingsClicked()
                }
            },
            onItemClicked = onItemClicked,
        )
    }
}

@Composable
private fun AssignmentsContent(
    isLoading: Boolean,
    assignments: Assignments,
    expandedAssignmentId: String?,
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onEditTaskClicked: (String) -> Unit,
    filters: List<Filter>,
    onFilterSelected: (Filter, FilterItem) -> Unit,
    modifier: Modifier = Modifier,
    menu: List<AssignmentsMenuItem>,
    onMenuSelected: (AssignmentsMenuItem) -> Unit,
    customSnoozeHours: String,
    customSnoozeMinutes: String,
    onCustomHoursEntered: (String) -> Unit,
    onCustomMinutesEntered: (String) -> Unit,
    onCustomTimeSet: (String) -> Unit,
    onCustomTimeCanceled: () -> Unit,
    onItemClicked: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.dimens.medium)
    ) {
        Spacer(
            modifier = Modifier.height(
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            )
        )
        FilterRow(
            filters,
            onFilterSelected = onFilterSelected,
            menu = menu,
            onMenuSelected = onMenuSelected,
            modifier = Modifier
        )
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
                modifier = Modifier
                    .fillMaxSize()
            ) {
                AssignmentsList(
                    assignments = assignments,
                    expandedAssignmentId = expandedAssignmentId,
                    onMarkAsDone = onMarkAsDone,
                    onMarkAsWontDo = onMarkAsWontDo,
                    onSnooze = onSnooze,
                    onEditTaskClicked = onEditTaskClicked,
                    customSnoozeHours = customSnoozeHours,
                    customSnoozeMinutes = customSnoozeMinutes,
                    onCustomHoursEntered = onCustomHoursEntered,
                    onCustomMinutesEntered = onCustomMinutesEntered,
                    onCustomTimeSet = onCustomTimeSet,
                    onCustomTimeCanceled = onCustomTimeCanceled,
                    onItemClicked = onItemClicked,
                )
            }
        }
    }
}

@Composable
private fun AssignmentsList(
    assignments: Assignments,
    expandedAssignmentId: String?,
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onEditTaskClicked: (String) -> Unit,
    customSnoozeHours: String,
    customSnoozeMinutes: String,
    onCustomHoursEntered: (String) -> Unit,
    onCustomMinutesEntered: (String) -> Unit,
    onCustomTimeSet: (String) -> Unit,
    onCustomTimeCanceled: () -> Unit,
    onItemClicked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
    ) {
        assignmentsGroup(
            headerRes = R.string.assignment_header_on_completion,
            assignments = assignments.onCompletion,
            expandedAssignmentId = expandedAssignmentId,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
            customSnoozeHours = customSnoozeHours,
            customSnoozeMinutes = customSnoozeMinutes,
            onCustomHoursEntered = onCustomHoursEntered,
            onCustomMinutesEntered = onCustomMinutesEntered,
            onCustomTimeSet = onCustomTimeSet,
            onCustomTimeCanceled = onCustomTimeCanceled,
            onItemClicked = onItemClicked,
        )
        assignmentsGroup(
            headerRes = R.string.assignment_header_past_due,
            assignments = assignments.pastDue,
            expandedAssignmentId = expandedAssignmentId,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
            customSnoozeHours = customSnoozeHours,
            customSnoozeMinutes = customSnoozeMinutes,
            onCustomHoursEntered = onCustomHoursEntered,
            onCustomMinutesEntered = onCustomMinutesEntered,
            onCustomTimeSet = onCustomTimeSet,
            onCustomTimeCanceled = onCustomTimeCanceled,
            onItemClicked = onItemClicked,
        )
        assignmentsGroup(
            headerRes = R.string.assignment_header_due_today,
            assignments = assignments.dueToday,
            expandedAssignmentId = expandedAssignmentId,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
            customSnoozeHours = customSnoozeHours,
            customSnoozeMinutes = customSnoozeMinutes,
            onCustomHoursEntered = onCustomHoursEntered,
            onCustomMinutesEntered = onCustomMinutesEntered,
            onCustomTimeSet = onCustomTimeSet,
            onCustomTimeCanceled = onCustomTimeCanceled,
            onItemClicked = onItemClicked,
        )
        assignmentsGroup(
            headerRes = R.string.assignment_header_due_tomorrow,
            assignments = assignments.dueTomorrow,
            expandedAssignmentId = expandedAssignmentId,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
            customSnoozeHours = customSnoozeHours,
            customSnoozeMinutes = customSnoozeMinutes,
            onCustomHoursEntered = onCustomHoursEntered,
            onCustomMinutesEntered = onCustomMinutesEntered,
            onCustomTimeSet = onCustomTimeSet,
            onCustomTimeCanceled = onCustomTimeCanceled,
            onItemClicked = onItemClicked,
        )
        assignmentsGroup(
            headerRes = R.string.assignment_header_due_in_future,
            assignments = assignments.dueInFuture,
            expandedAssignmentId = expandedAssignmentId,
            otherAssignmentsCount = assignments.otherAssignmentsCount,
            onMarkAsDone = onMarkAsDone,
            onMarkAsWontDo = onMarkAsWontDo,
            onSnooze = onSnooze,
            onEditTaskClicked = onEditTaskClicked,
            customSnoozeHours = customSnoozeHours,
            customSnoozeMinutes = customSnoozeMinutes,
            onCustomHoursEntered = onCustomHoursEntered,
            onCustomMinutesEntered = onCustomMinutesEntered,
            onCustomTimeSet = onCustomTimeSet,
            onCustomTimeCanceled = onCustomTimeCanceled,
            onItemClicked = onItemClicked,
        )
        item {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.extraLarge))
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.extraLarge))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.assignmentsGroup(
    @StringRes headerRes: Int,
    assignments: List<TaskAssignmentDetails>,
    expandedAssignmentId: String?,
    otherAssignmentsCount: Map<String, Int>,
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onEditTaskClicked: (String) -> Unit,
    customSnoozeHours: String,
    customSnoozeMinutes: String,
    onCustomHoursEntered: (String) -> Unit,
    onCustomMinutesEntered: (String) -> Unit,
    onCustomTimeSet: (String) -> Unit,
    onCustomTimeCanceled: () -> Unit,
    onItemClicked: (String) -> Unit,
) {
    if (assignments.isNotEmpty()) {
        stickyHeader {
            AssignmentHeader(stringResource(id = headerRes))
        }
        items(assignments, key = { it.taskAssignment.id }) { item ->
            AssignmentItem(
                details = item,
                expanded = expandedAssignmentId == item.taskAssignment.id,
                otherAssignmentsCount = otherAssignmentsCount[item.taskAssignment.id]
                    ?: 0,
                onMarkAsDone = onMarkAsDone,
                onMarkAsWontDo = onMarkAsWontDo,
                onSnooze = onSnooze,
                onEditTaskClicked = onEditTaskClicked,
                customSnoozeHours = customSnoozeHours,
                customSnoozeMinutes = customSnoozeMinutes,
                onCustomHoursEntered = onCustomHoursEntered,
                onCustomMinutesEntered = onCustomMinutesEntered,
                onCustomTimeSet = onCustomTimeSet,
                onCustomTimeCanceled = onCustomTimeCanceled,
                onItemClicked = onItemClicked,
            )
        }
    }
}

@Composable
private fun AssignmentHeader(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = MaterialTheme.dimens.small)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignmentItem(
    details: TaskAssignmentDetails,
    expanded: Boolean,
    otherAssignmentsCount: Int,
    onMarkAsDone: (String) -> Unit,
    onMarkAsWontDo: (String) -> Unit,
    onSnooze: (String, SnoozeType) -> Unit,
    onEditTaskClicked: (String) -> Unit,
    customSnoozeHours: String,
    customSnoozeMinutes: String,
    onCustomHoursEntered: (String) -> Unit,
    onCustomMinutesEntered: (String) -> Unit,
    onCustomTimeSet: (String) -> Unit,
    onCustomTimeCanceled: () -> Unit,
    onItemClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Card(
        onClick = { onItemClicked(details.taskAssignment.id) },
        border = if (details.assignedToLoggedInUser) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else {
            BorderStroke(
                0.1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = ContentAlpha.disabled)
            )
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = modifier
            .animateContentSize()
    ) {
        Row {
            Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
            FilledTonalIconButton(
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    onMarkAsDone(details.taskAssignment.id)
                },
                modifier = Modifier
                    .width(MaterialTheme.dimens.iconWidthLarge)
                    .align(alignment = Alignment.CenterVertically),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.ok)
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
                    if (details.taskAssignment.repeatInfo.repeatUnit
                        != RepeatUnit.ON_COMPLETE
                    ) {
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val text = details.taskAssignment.memberName
                    LabelWithIcon(text = text, icon = Icons.Filled.Person)
                    if (details.taskAssignment.repeatInfo.repeatUnit != RepeatUnit.NONE
                        && details.taskAssignment.repeatInfo.repeatValue != 0
                    ) {
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
                        val repeatTextValue = formatRepeatUnitCompact(
                            repeatValue = details.taskAssignment.repeatInfo.repeatValue,
                            repeatUnit = details.taskAssignment.repeatInfo.repeatUnit
                        )
                        LabelWithIcon(text = repeatTextValue, icon = Icons.Filled.Repeat)
                    }
                    if (otherAssignmentsCount > 0) {
                        Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
                        LabelWithIcon(
                            text = pluralStringResource(
                                id = R.plurals.assignment_iterations_more,
                                count = otherAssignmentsCount,
                                otherAssignmentsCount
                            ),
                            icon = Icons.Filled.GroupWork
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
                if (expanded) {
                    if (details.willReminderBeSet && details.reminderTime != null) {
                        Row(Modifier.padding(horizontal = MaterialTheme.dimens.medium)) {
                            LabelWithIcon(
                                text = formatReminderAt(toFormat = details.reminderTime),
                                icon = Icons.Filled.Alarm
                            )
                        }
                        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
                    }
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.dimens.medium),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
                    ) {
                        item {
                            FilledTonalButton(
                                onClick = { onEditTaskClicked(details.taskAssignment.taskId) },
                                contentPadding = PaddingValues(MaterialTheme.dimens.medium)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    modifier = Modifier.size(MaterialTheme.dimens.iconWidthSmall),
                                    contentDescription = stringResource(id = R.string.edit)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
                                Text(text = stringResource(id = R.string.edit))
                            }
                        }
                        if (details.enableSnooze) {
                            item {
                                SnoozeButton(
                                    onSnooze = { snoozeType ->
                                        onSnooze(details.taskAssignment.id, snoozeType)
                                    },
                                    hours = customSnoozeHours,
                                    minutes = customSnoozeMinutes,
                                    onCustomHoursEntered = onCustomHoursEntered,
                                    onCustomMinutesEntered = onCustomMinutesEntered,
                                    onCustomTimeSet = {
                                        onCustomTimeSet(details.taskAssignment.id)
                                    },
                                    onCustomTimeCanceled = onCustomTimeCanceled,
                                )
                            }
                        }
                        item {
                            SecondaryButton(
                                icon = Icons.Default.Clear,
                                textResId = R.string.assignment_details_button_wont_do,
                                onClick = { onMarkAsWontDo(details.taskAssignment.id) },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
                }
            }
        }
    }
}

@Composable
private fun SecondaryButton(
    icon: ImageVector,
    @StringRes textResId: Int,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        contentPadding = PaddingValues(MaterialTheme.dimens.medium),
    ) {
        Icon(
            imageVector = icon,
            modifier = Modifier.size(MaterialTheme.dimens.iconWidthSmall),
            contentDescription = stringResource(id = textResId)
        )
        Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
        Text(text = stringResource(id = textResId))
    }
}

@Composable
private fun SnoozeButton(
    hours: String,
    minutes: String,
    onCustomHoursEntered: (String) -> Unit,
    onCustomMinutesEntered: (String) -> Unit,
    onCustomTimeSet: () -> Unit,
    onCustomTimeCanceled: () -> Unit,
    onSnooze: (SnoozeType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showCustomSnoozeDialog by remember { mutableStateOf(false) }
    SecondaryButton(
        icon = Icons.Default.AlarmAdd,
        textResId = R.string.assignment_details_button_snooze,
        onClick = { expanded = true },
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(
                        id = R.string.assignment_details_button_snooze_six_hours
                    )
                )
            },
            onClick = {
                expanded = false
                onSnooze(SnoozeType.SixHours)
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(
                        id = R.string.assignment_details_button_snooze_tomorrow_morning
                    )
                )
            },
            onClick = {
                expanded = false
                onSnooze(SnoozeType.TomorrowMorning)
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    stringResource(
                        id = R.string.assignment_details_button_snooze_custom
                    )
                )
            },
            onClick = {
                expanded = false
                showCustomSnoozeDialog = true
            }
        )
    }

    if (showCustomSnoozeDialog) {
        CustomSnoozeDialog(
            hours = hours,
            minutes = minutes,
            onCustomHoursEntered = onCustomHoursEntered,
            onCustomMinutesEntered = onCustomMinutesEntered,
            onCustomTimeSet = {
                onCustomTimeSet()
                showCustomSnoozeDialog = false
            },
            onDismissRequested = {
                onCustomTimeCanceled()
                showCustomSnoozeDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomSnoozeDialog(
    hours: String,
    minutes: String,
    onCustomHoursEntered: (String) -> Unit,
    onCustomMinutesEntered: (String) -> Unit,
    onCustomTimeSet: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(focusRequester) {
        delay(100)
        focusRequester.requestFocus()
        keyboard?.show()
    }
    AlertDialog(
        onDismissRequest = onDismissRequested,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(MaterialTheme.dimens.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = hours,
                    singleLine = true,
                    onValueChange = onCustomHoursEntered,
                    label = {
                        Text(stringResource(id = R.string.assignment_details_button_snooze_hours))
                    },
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester = focusRequester)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
                OutlinedTextField(
                    value = minutes,
                    singleLine = true,
                    onValueChange = onCustomMinutesEntered,
                    label = {
                        Text(stringResource(id = R.string.assignment_details_button_snooze_minutes))
                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onCustomTimeSet()
                            onDismissRequested()
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
                Text(
                    text = stringResource(id = R.string.assignment_details_snooze_max_time_message),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            onDismissRequested()
                        }
                    ) { Text(stringResource(id = R.string.cancel)) }
                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            onCustomTimeSet()
                            onDismissRequested()
                        }
                    ) { Text(stringResource(id = R.string.ok)) }
                }
            }
        }
    }
}

@Composable
private fun LabelWithIcon(text: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .background(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            .padding(MaterialTheme.dimens.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(MaterialTheme.dimens.large)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = MaterialTheme.dimens.small)
        )
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
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
fun FilterRow(
    filters: List<Filter>,
    onFilterSelected: (Filter, FilterItem) -> Unit,
    menu: List<AssignmentsMenuItem>,
    onMenuSelected: (AssignmentsMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium),
            modifier = Modifier.weight(1F)
        ) {
            items(filters, key = { it.getKey() }) { item ->
                FilterOption(filter = item, onFilterSelected = onFilterSelected)
            }
        }
        MoreMenu(menu, onMenuSelected)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOption(
    filter: Filter,
    onFilterSelected: (Filter, FilterItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val (optionIcon, contentDescription) = when (filter.getType()) {
        FilterType.PERSON ->
            Pair(Icons.Filled.Person, R.string.assignment_filter_person_content_description)

        FilterType.HOUSE ->
            Pair(Icons.Filled.House, R.string.assignment_filter_house_content_description)
    }
    Box {
        FilterChip(
            selected = filter.getItems().any { it.getIsSelected() },
            onClick = { expanded = !expanded },
            label = { Text(filter.getDisplayText().string()) },
            leadingIcon = {
                Icon(
                    imageVector = optionIcon,
                    contentDescription = stringResource(id = contentDescription),
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(id = contentDescription),
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            filter.getItems().forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.getDisplayName().string()) },
                    trailingIcon = {
                        Icon(
                            imageVector = if (item.getIsSelected()) {
                                Icons.Filled.CheckBox
                            } else {
                                Icons.Filled.CheckBoxOutlineBlank
                            },
                            contentDescription = stringResource(id = contentDescription),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    onClick = {
                        expanded = false
                        onFilterSelected(filter, item)
                    }
                )
            }
        }
    }
}

@Composable
fun MoreMenu(
    menu: List<AssignmentsMenuItem>,
    onMenuSelected: (AssignmentsMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(
            onClick = {
                expanded = !expanded
            },
            modifier = Modifier
                .size(MaterialTheme.dimens.iconWidthLarge)
                .padding(MaterialTheme.dimens.small)
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(id = R.string.assignment_menu_content_description)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            menu.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.text.string()) },
                    onClick = {
                        expanded = false
                        onMenuSelected(item)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewIcon() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.List,
        contentDescription = stringResource(id = R.string.assignment_filter_person_content_description),
        modifier = Modifier.size(64.dp)
    )
}

@Preview
@Composable
fun PreviewFilterOption() {
    Surface {
        FilterOption(
            filter = PersonFilter(
                text = TextValue.ForString("Jess"),
                items = listOf(
                    PersonFilterItem(
                        id = "1",
                        displayName = TextValue.ForString("Ramit"),
                        selected = false
                    ),
                    PersonFilterItem(
                        id = "2",
                        displayName = TextValue.ForString("Jess"),
                        selected = true
                    ),
                    PersonFilterItem(
                        id = "3",
                        displayName = TextValue.ForString("All"),
                        selected = false
                    )
                )
            )
        ) { filter, filterItem ->
            println(filter.getDisplayText().toString() + " " + filterItem.getDisplayName())
        }
    }
}

@Preview
@Composable
fun PreviewAssignmentContentContentEmpty() {
    ChoresClientTheme {
        Surface {
            AssignmentsContent(
                isLoading = false,
                assignments = Assignments.default(),
                expandedAssignmentId = null,
                onMarkAsDone = { },
                onMarkAsWontDo = { },
                onSnooze = { _, _ -> },
                onEditTaskClicked = { },
                filters = listOf(),
                onFilterSelected = { _, _ -> },
                menu = listOf(AssignmentsMenuItem.SETTINGS),
                onMenuSelected = {},
                customSnoozeHours = "",
                customSnoozeMinutes = "",
                onCustomHoursEntered = {},
                onCustomMinutesEntered = {},
                onCustomTimeSet = {},
                onCustomTimeCanceled = {},
                onItemClicked = {},
            )
        }
    }
}

@Preview
@Composable
fun PreviewEmptyContent() {
    Surface {
        EmptyContent()
    }
}

@Preview
@Composable
private fun PreviewAssignmentContent() {
    val viewState = AssignmentsViewState(
        loading = false,
        assignments = Assignments(
            onCompletion = listOf(
                TaskAssignmentDetails(
                    taskAssignment = TaskAssignment(
                        id = "1",
                        progressStatus = ProgressStatus.TODO,
                        progressStatusDate = Clock.System.now(),
                        taskId = "taskId",
                        taskName = "Task",
                        houseId = "houseId",
                        repeatInfo = RepeatInfo(
                            repeatValue = 1,
                            repeatUnit = RepeatUnit.ON_COMPLETE,
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
                )
            ),
            pastDue = listOf(
                TaskAssignmentDetails(
                    taskAssignment = TaskAssignment(
                        id = "2",
                        progressStatus = ProgressStatus.TODO,
                        progressStatusDate = Clock.System.now(),
                        taskId = "taskId",
                        taskName = "Task",
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
                        id = "3",
                        progressStatus = ProgressStatus.TODO,
                        progressStatusDate = Clock.System.now(),
                        taskId = "taskId",
                        taskName = "Task",
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
            dueToday = listOf(
                TaskAssignmentDetails(
                    taskAssignment = TaskAssignment(
                        id = "4",
                        progressStatus = ProgressStatus.TODO,
                        progressStatusDate = Clock.System.now(),
                        taskId = "taskId",
                        taskName = "Task",
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
            dueTomorrow = listOf(
                TaskAssignmentDetails(
                    taskAssignment = TaskAssignment(
                        id = "5",
                        progressStatus = ProgressStatus.TODO,
                        progressStatusDate = Clock.System.now(),
                        taskId = "taskId",
                        taskName = "Task",
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
                )
            ),
            dueInFuture = listOf(
                TaskAssignmentDetails(
                    taskAssignment = TaskAssignment(
                        id = "6",
                        progressStatus = ProgressStatus.TODO,
                        progressStatusDate = Clock.System.now(),
                        taskId = "taskId",
                        taskName = "Task",
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
            otherAssignmentsCount = mapOf()
        ),
        filters = listOf()
    )
    Surface {
        AssignmentsScreen(
            viewState = viewState,
            onMarkAsDone = { },
            onMarkAsWontDo = { },
            onSnooze = { _, _ -> },
            onFilterItemClicked = { _, _ -> },
            onAddTaskClicked = { },
            onEditTaskClicked = { },
            onSettingsClicked = { },
            onCustomHoursEntered = {},
            onCustomMinutesEntered = {},
            onCustomTimeSet = {},
            onCustomTimeCanceled = {},
            onItemClicked = {}
        )
    }
}
