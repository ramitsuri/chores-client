package com.ramitsuri.choresclient.android.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatRepeatUnitCompact
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.EditTaskViewState
import com.ramitsuri.choresclient.model.view.RepeatUnitSelectionItem
import com.ramitsuri.choresclient.model.view.SelectionItem
import com.ramitsuri.choresclient.utils.formatDate
import com.ramitsuri.choresclient.utils.formatTime
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime


@Composable
fun EditTaskScreen(
    viewState: EditTaskViewState,
    onTaskNameUpdate: (String) -> Unit,
    onDatePicked: (Long) -> Unit,
    onTimePicked: (Int, Int) -> Unit,
    onRepeatValueUpdated: (String) -> Unit,
    onRepeatUnitSelected: (SelectionItem) -> Unit,
    onRotateMemberClicked: () -> Unit,
    onRepeatEndDatePicked: (Long) -> Unit,
    onResetRepeatInfo: () -> Unit,
    onEditTaskRequested: () -> Unit,
    onActiveStatusSelected: (ActiveStatus) -> Unit,
    onResetActiveStatus: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(viewState, focusRequester) {
        if (viewState.taskName.isNotEmpty() && !viewState.loading) {
            delay(100)
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
            keyboard?.show()
        }
    }
    Surface {
        Column(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = MaterialTheme.dimens.medium),
        ) {

            if (viewState.loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } else {
                EditTaskContent(
                    state = viewState,
                    onTaskNameUpdate = onTaskNameUpdate,
                    onDatePicked = onDatePicked,
                    onTimePicked = onTimePicked,
                    onRepeatValueUpdated = onRepeatValueUpdated,
                    onRepeatUnitSelected = onRepeatUnitSelected,
                    onRotateMemberClicked = onRotateMemberClicked,
                    onRepeatEndDatePicked = onRepeatEndDatePicked,
                    onResetRepeatInfo = onResetRepeatInfo,
                    onEditTaskRequested = {
                        keyboard?.hide()
                        onEditTaskRequested()
                    },
                    onActiveStatusSelected = onActiveStatusSelected,
                    onResetActiveStatus = onResetActiveStatus,
                    onBack = onBack,
                    focusManager = focusManager,
                    focusRequester = focusRequester
                )
            }
        }
    }
}

@Composable
private fun EditTaskContent(
    modifier: Modifier = Modifier,
    state: EditTaskViewState,
    onTaskNameUpdate: (String) -> Unit,
    onDatePicked: (Long) -> Unit,
    onTimePicked: (Int, Int) -> Unit,
    onRepeatValueUpdated: (String) -> Unit,
    onRepeatUnitSelected: (SelectionItem) -> Unit,
    onRotateMemberClicked: () -> Unit,
    onRepeatEndDatePicked: (Long) -> Unit,
    onResetRepeatInfo: () -> Unit,
    onEditTaskRequested: () -> Unit,
    onActiveStatusSelected: (ActiveStatus) -> Unit,
    onResetActiveStatus: () -> Unit,
    onBack: () -> Unit,
    focusManager: FocusManager,
    focusRequester: FocusRequester,
) {
    var taskNameSelection by remember{ mutableStateOf(TextRange(state.taskName.length)) }
    Column(modifier = modifier) {
        BackButton(onBack = onBack)
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
        OutlinedTextField(
            value = TextFieldValue(
                text = state.taskName,
                selection = taskNameSelection
            ),
            singleLine = true,
            onValueChange = {
                onTaskNameUpdate(it.text)
                taskNameSelection = it.selection
            },
            label = { Text(stringResource(id = R.string.add_task_hint_name)) },
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = focusRequester)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
        Row(modifier = Modifier.fillMaxWidth()) {
            var showDatePickerState by remember { mutableStateOf(false) }
            var showTimePickerState by remember { mutableStateOf(false) }

            PickerItemButton(
                text = formatDate(state.date, now = state.now, timeZone = state.timeZone),
                startIcon = Icons.Filled.CalendarToday,
                onClick = { showDatePickerState = true },
                smallerSize = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(MaterialTheme.dimens.small)
            )
            PickerItemButton(
                text = formatTime(state.time),
                startIcon = Icons.Filled.AccessTime,
                onClick = { showTimePickerState = true },
                smallerSize = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(MaterialTheme.dimens.small)
            )

            if (showDatePickerState) {
                DatePicker(
                    now = state.now,
                    selectedDate = state.date,
                    timeZone = state.timeZone,
                    onDismissRequested = { showDatePickerState = false },
                    onDatePicked = onDatePicked
                )
            }

            if (showTimePickerState) {
                TimePicker(
                    time = state.time,
                    onDismissRequested = { showTimePickerState = false },
                    onTimePicked = onTimePicked
                )
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
        Row(modifier = Modifier.fillMaxWidth()) {
            var showRepeatInfoState by remember { mutableStateOf(false) }
            var showActiveStatusState by remember { mutableStateOf(false) }

            PickerItemButton(
                text = formatRepeatUnitCompact(
                    repeatValue = state.repeatValue,
                    repeatUnit = state.repeatUnits.first { it.getIsSelected() }.toRepeatUnit()
                ),
                startIcon = Icons.Filled.Repeat,
                onClick = { showRepeatInfoState = true },
                smallerSize = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(MaterialTheme.dimens.small)
            )

            PickerItemButton(
                text = state.status.label,
                startIcon = state.status.icon,
                onClick = { showActiveStatusState = true },
                smallerSize = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(MaterialTheme.dimens.small)
            )

            if (showRepeatInfoState) {
                RepeatInfoDialog(
                    repeatValue = state.repeatValue,
                    repeatUnits = state.repeatUnits,
                    onRepeatValueUpdated = onRepeatValueUpdated,
                    onRepeatUnitSelected = onRepeatUnitSelected,
                    rotateChecked = state.rotateMember,
                    onRotateMemberClicked = onRotateMemberClicked,
                    now = state.now,
                    repeatEndDate = state.repeatEndDate,
                    timeZone = state.timeZone,
                    onDismissRequested = { showRepeatInfoState = false },
                    onRepeatEndDatePicked = onRepeatEndDatePicked,
                    onResetRepeatInfo = onResetRepeatInfo,
                    focusManager = focusManager,
                )
            }

            if (showActiveStatusState) {
                ActiveStatusDialog(
                    selectedActiveStatus = state.status,
                    onActiveStatusSelected = onActiveStatusSelected,
                    onResetActiveStatus = onResetActiveStatus,
                    onDismissRequested = { showActiveStatusState = false }
                )
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
        FilledTonalButton(
            onClick = onEditTaskRequested,
            enabled = state.enableEditTask,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.edit_task_edit))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveStatusDialog(
    selectedActiveStatus: ActiveStatus,
    onActiveStatusSelected: (ActiveStatus) -> Unit,
    onResetActiveStatus: () -> Unit,
    onDismissRequested: () -> Unit,
) {
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
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.edit_task_active_status_title),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .verticalScroll(rememberScrollState())
                ) {
                    ActiveStatus
                        .values()
                        .filter { it != ActiveStatus.UNKNOWN }
                        .forEach { activeStatus ->
                            RadioButtonItem(
                                text = activeStatus.label,
                                hint = activeStatus.hint,
                                selected = activeStatus == selectedActiveStatus,
                                onClick = { onActiveStatusSelected(activeStatus) }
                            )
                            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
                        }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            onResetActiveStatus()
                            onDismissRequested()
                        }
                    ) { Text(stringResource(id = R.string.reset)) }
                    TextButton(
                        onClick = {
                            onDismissRequested()
                        }
                    ) { Text(stringResource(id = R.string.ok)) }
                }
            }
        }
    }
}

@Composable
fun RadioButtonItem(
    text: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = MaterialTheme.dimens.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
        Column {
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
            Text(
                text = hint,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal)
            )
        }
    }


}

private val ActiveStatus.label: String
    @Composable
    get() = when (this) {
        ActiveStatus.UNKNOWN -> {
            throw IllegalStateException("Invalid active status")
        }

        ActiveStatus.ACTIVE -> {
            stringResource(id = R.string.edit_task_active_status_active)
        }

        ActiveStatus.INACTIVE -> {
            stringResource(id = R.string.edit_task_active_status_inactive)
        }

        ActiveStatus.PAUSED -> {
            stringResource(id = R.string.edit_task_active_status_pause)
        }
    }

private val ActiveStatus.hint: String
    @Composable
    get() = when (this) {
        ActiveStatus.UNKNOWN -> {
            throw IllegalStateException("Invalid active status")
        }

        ActiveStatus.ACTIVE -> {
            stringResource(id = R.string.edit_task_active_status_active_hint)
        }

        ActiveStatus.INACTIVE -> {
            stringResource(id = R.string.edit_task_active_status_inactive_hint)
        }

        ActiveStatus.PAUSED -> {
            stringResource(id = R.string.edit_task_active_status_pause_hint)
        }
    }

private val ActiveStatus.icon: ImageVector
    get() = when (this) {
        ActiveStatus.UNKNOWN -> {
            throw IllegalStateException("Invalid active status")
        }

        ActiveStatus.ACTIVE -> {
            Icons.Filled.PlayArrow
        }

        ActiveStatus.INACTIVE -> {
            Icons.Filled.Stop
        }

        ActiveStatus.PAUSED -> {
            Icons.Filled.Pause
        }
    }

@Preview
@Composable
fun EditTasksPreview() {
    val viewState = EditTaskViewState(
        loading = false,
        taskName = "",
        repeatValue = 2,
        repeatUnits = RepeatUnit.values().mapIndexed { index, repeatUnit ->
            RepeatUnitSelectionItem(repeatUnit, selected = index == 1)
        },
        repeatEndDate = null,
        date = LocalDate.parse("2023-09-13"),
        time = LocalTime.parse("13:00:00"),
        rotateMember = false,
        error = null,
        enableEditTask = false,
    )
    EditTaskScreen(
        viewState = viewState,
        onBack = { },
        onTaskNameUpdate = { },
        onDatePicked = { _ -> },
        onTimePicked = { _, _ -> },
        onRepeatValueUpdated = {},
        onRepeatUnitSelected = {},
        onRotateMemberClicked = {},
        onRepeatEndDatePicked = {},
        onResetRepeatInfo = {},
        onEditTaskRequested = {},
        onActiveStatusSelected = {},
        onResetActiveStatus = {},
    )
}

@Preview
@Composable
fun EditTasksLoadingPreview() {
    val viewState = EditTaskViewState(
        loading = true,
        taskName = "",
        repeatValue = 0,
        repeatUnits = listOf(),
        repeatEndDate = null,
        date = LocalDate.parse("2023-09-13"),
        time = LocalTime.parse("13:00:00"),
        rotateMember = false,
        error = null,
        enableEditTask = false,
    )
    EditTaskScreen(
        viewState = viewState,
        onBack = { },
        onTaskNameUpdate = { },
        onDatePicked = { _ -> },
        onTimePicked = { _, _ -> },
        onRepeatValueUpdated = {},
        onRepeatUnitSelected = {},
        onRotateMemberClicked = {},
        onRepeatEndDatePicked = {},
        onResetRepeatInfo = {},
        onEditTaskRequested = {},
        onActiveStatusSelected = {},
        onResetActiveStatus = {},
    )
}