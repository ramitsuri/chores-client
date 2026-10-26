package com.ramitsuri.choresclient.android.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.common.AppTopAppBar
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatRepeatUnitCompact
import com.ramitsuri.choresclient.model.entities.House
import com.ramitsuri.choresclient.model.entities.Member
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.AddTaskViewState
import com.ramitsuri.choresclient.model.view.HouseSelectionItem
import com.ramitsuri.choresclient.model.view.MemberSelectionItem
import com.ramitsuri.choresclient.model.view.RepeatUnitSelectionItem
import com.ramitsuri.choresclient.model.view.SelectionItem
import com.ramitsuri.choresclient.utils.formatDate
import com.ramitsuri.choresclient.utils.formatTime
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
fun AddTaskScreen(
    viewState: AddTaskViewState,
    onTaskNameUpdate: (String) -> Unit,
    onTaskDescriptionUpdate: (String) -> Unit,
    onHouseItemSelected: (SelectionItem) -> Unit,
    onMemberItemSelected: (SelectionItem) -> Unit,
    onDatePicked: (Long) -> Unit,
    onTimePicked: (Int, Int) -> Unit,
    onRepeatValueUpdated: (String) -> Unit,
    onRepeatUnitSelected: (SelectionItem) -> Unit,
    onRotateMemberClicked: () -> Unit,
    onRepeatEndDatePicked: (Long) -> Unit,
    onResetRepeatInfo: () -> Unit,
    onAddTaskRequested: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusRequester) {
        if (!viewState.loading) {
            delay(100)
            try {
                focusRequester.requestFocus()
                keyboard?.show()
            } catch (_: Exception) {
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.new_todo_title),
                onBack = onBack
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.dimens.large),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
                Button(
                    onClick = {
                        keyboard?.hide()
                        onAddTaskRequested()
                    },
                    enabled = viewState.enableAddTask,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.save),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .fillMaxSize()
            .imePadding()
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
                .verticalScroll(rememberScrollState())
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
                AddTaskContent(
                    state = viewState,
                    onTaskNameUpdate = onTaskNameUpdate,
                    onTaskDescriptionUpdate = onTaskDescriptionUpdate,
                    onHouseItemSelected = onHouseItemSelected,
                    onMemberItemSelected = onMemberItemSelected,
                    onDatePicked = onDatePicked,
                    onTimePicked = onTimePicked,
                    onRepeatValueUpdated = onRepeatValueUpdated,
                    onRepeatUnitSelected = onRepeatUnitSelected,
                    onRotateMemberClicked = onRotateMemberClicked,
                    onRepeatEndDatePicked = onRepeatEndDatePicked,
                    onResetRepeatInfo = onResetRepeatInfo,
                    focusManager = focusManager,
                    focusRequester = focusRequester
                )
            }
        }
    }
}

@Composable
private fun AddTaskContent(
    modifier: Modifier = Modifier,
    state: AddTaskViewState,
    onTaskNameUpdate: (String) -> Unit,
    onTaskDescriptionUpdate: (String) -> Unit,
    onHouseItemSelected: (SelectionItem) -> Unit,
    onMemberItemSelected: (SelectionItem) -> Unit,
    onDatePicked: (Long) -> Unit,
    onTimePicked: (Int, Int) -> Unit,
    onRepeatValueUpdated: (String) -> Unit,
    onRepeatUnitSelected: (SelectionItem) -> Unit,
    onRotateMemberClicked: () -> Unit,
    onRepeatEndDatePicked: (Long) -> Unit,
    onResetRepeatInfo: () -> Unit,
    focusManager: FocusManager,
    focusRequester: FocusRequester,
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

        // Title Field
        OutlinedTextField(
            value = state.taskName,
            singleLine = true,
            onValueChange = onTaskNameUpdate,
            label = { Text(stringResource(id = R.string.add_task_hint_name)) },
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = focusRequester)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

        // Description Field
        OutlinedTextField(
            value = state.taskDescription,
            minLines = 3,
            maxLines = 5,
            onValueChange = onTaskDescriptionUpdate,
            label = { Text(stringResource(id = R.string.add_task_hint_description)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))

        // Destination Selector (House & Member)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
        ) {
            if (state.houses.isNotEmpty()) {
                SelectionMenu(
                    startIcon = Icons.Filled.List,
                    items = state.houses,
                    onItemSelected = onHouseItemSelected,
                    modifier = Modifier.weight(1f)
                )
            }
            if (state.members.isNotEmpty()) {
                SelectionMenu(
                    startIcon = Icons.Filled.Person,
                    items = state.members,
                    onItemSelected = onMemberItemSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))

        // Due Date, Time & Recurrence Pickers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.small)
        ) {
            var showDatePickerState by remember { mutableStateOf(false) }
            var showTimePickerState by remember { mutableStateOf(false) }
            var showRepeatInfoState by remember { mutableStateOf(false) }

            PickerItemButton(
                text = formatDate(state.date, now = state.now, timeZone = state.timeZone),
                startIcon = Icons.Filled.CalendarToday,
                onClick = { showDatePickerState = true },
                smallerSize = true,
                modifier = Modifier.weight(1f)
            )
            PickerItemButton(
                text = formatTime(state.time),
                startIcon = Icons.Filled.AccessTime,
                onClick = { showTimePickerState = true },
                smallerSize = true,
                modifier = Modifier.weight(1f)
            )
            PickerItemButton(
                text = formatRepeatUnitCompact(
                    repeatValue = state.repeatValue,
                    repeatUnit = state.repeatUnits.first { it.getIsSelected() }.toRepeatUnit()
                ),
                startIcon = Icons.Filled.Repeat,
                onClick = { showRepeatInfoState = true },
                smallerSize = true,
                modifier = Modifier.weight(1f)
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
                TaskTimePickerDialog(
                    currentTime = state.time,
                    morningTime = state.morningTime,
                    eveningTime = state.eveningTime,
                    onDismissRequested = { showTimePickerState = false },
                    onTimePicked = onTimePicked
                )
            }

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
        }
    }
}

@Preview
@Composable
fun AddTasksPreview() {
    val viewState = AddTaskViewState(
        loading = false,
        taskName = "",
        taskDescription = "",
        repeatValue = 2,
        repeatUnits = RepeatUnit.values().mapIndexed { index, repeatUnit ->
            RepeatUnitSelectionItem(repeatUnit, selected = index == 1)
        },
        repeatEndDate = null,
        houses = listOf(
            HouseSelectionItem(
                house = House(
                    id = "1",
                    name = "Personal",
                    createdByMemberId = "1",
                    createdDate = Clock.System.now(),
                    status = ActiveStatus.ACTIVE
                ),
                selected = true
            )
        ),
        members = listOf(
            MemberSelectionItem(
                member = Member(
                    id = "1",
                    name = "You",
                ),
                selected = true
            )
        ),
        date = LocalDate.parse("2023-09-13"),
        time = LocalTime.parse("13:00:00"),
        rotateMember = false,
        error = null,
        enableAddTask = false,
    )
    ChoresClientTheme {
        Surface {
            AddTaskScreen(
                viewState = viewState,
                onBack = { },
                onTaskNameUpdate = { },
                onTaskDescriptionUpdate = { },
                onHouseItemSelected = { _ -> },
                onMemberItemSelected = { _ -> },
                onDatePicked = { _ -> },
                onTimePicked = { _, _ -> },
                onRepeatValueUpdated = {},
                onRepeatUnitSelected = {},
                onRotateMemberClicked = {},
                onRepeatEndDatePicked = {},
                onResetRepeatInfo = {},
                onAddTaskRequested = {},
            )
        }
    }
}
