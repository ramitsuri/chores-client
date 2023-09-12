package com.ramitsuri.choresclient.android.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.ramitsuri.choresclient.android.R
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
fun AddTaskScreen(
    viewState: AddTaskViewState,
    onTaskNameUpdate: (String) -> Unit,
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
            focusRequester.requestFocus()
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
                AddTaskContent(
                    state = viewState,
                    onTaskNameUpdate = onTaskNameUpdate,
                    onHouseItemSelected = onHouseItemSelected,
                    onMemberItemSelected = onMemberItemSelected,
                    onDatePicked = onDatePicked,
                    onTimePicked = onTimePicked,
                    onRepeatValueUpdated = onRepeatValueUpdated,
                    onRepeatUnitSelected = onRepeatUnitSelected,
                    onRotateMemberClicked = onRotateMemberClicked,
                    onRepeatEndDatePicked = onRepeatEndDatePicked,
                    onResetRepeatInfo = onResetRepeatInfo,
                    onAddTaskRequested = {
                        keyboard?.hide()
                        onAddTaskRequested()
                    },
                    onBack = onBack,
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
    focusManager: FocusManager,
    focusRequester: FocusRequester,
) {
    Column(modifier = modifier) {
        BackButton(onBack = onBack)
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
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
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = focusRequester)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
        Row(modifier = Modifier.fillMaxWidth()) {
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
        Row(modifier = Modifier.fillMaxWidth()) {
            var showDatePickerState by remember { mutableStateOf(false) }
            var showTimePickerState by remember { mutableStateOf(false) }
            var showRepeatInfoState by remember { mutableStateOf(false) }

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
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
        FilledTonalButton(
            onClick = onAddTaskRequested,
            enabled = state.enableAddTask,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.add_task_add))
        }
    }
}

@Preview
@Composable
fun AddTasksPreview() {
    val viewState = AddTaskViewState(
        loading = false,
        taskName = "",
        repeatValue = 2,
        repeatUnits = RepeatUnit.values().mapIndexed { index, repeatUnit ->
            RepeatUnitSelectionItem(repeatUnit, selected = index == 1)
        },
        repeatEndDate = null,
        houses = listOf(
            HouseSelectionItem(
                house = House(
                    id = "1",
                    name = "List1",
                    createdByMemberId = "1",
                    createdDate = Clock.System.now(),
                    status = ActiveStatus.ACTIVE
                ),
                selected = true
            ),
            HouseSelectionItem(
                house = House(
                    id = "2",
                    name = "List2",
                    createdByMemberId = "1",
                    createdDate = Clock.System.now(),
                    status = ActiveStatus.ACTIVE
                ),
                selected = false
            ),
            HouseSelectionItem(
                house = House(
                    id = "3",
                    name = "List3",
                    createdByMemberId = "1",
                    createdDate = Clock.System.now(),
                    status = ActiveStatus.ACTIVE
                ),
                selected = false
            )
        ),
        members = listOf(
            MemberSelectionItem(
                member = Member(
                    id = "1",
                    name = "Member1",
                ),
                selected = true
            ),
            MemberSelectionItem(
                member = Member(
                    id = "2",
                    name = "Member2",
                ),
                selected = false
            ),
            MemberSelectionItem(
                member = Member(
                    id = "3",
                    name = "Member3",
                ),
                selected = false
            )
        ),
        date = LocalDate.parse("2023-09-13"),
        time = LocalTime.parse("13:00:00"),
        rotateMember = false,
        error = null,
        enableAddTask = false,
    )
    AddTaskScreen(
        viewState = viewState,
        onBack = { },
        onTaskNameUpdate = { },
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

@Preview
@Composable
fun AddTasksLoadingPreview() {
    val viewState = AddTaskViewState(
        loading = true,
        taskName = "",
        repeatValue = 0,
        repeatUnits = listOf(),
        repeatEndDate = null,
        houses = listOf(),
        members = listOf(),
        date = LocalDate.parse("2023-09-13"),
        time = LocalTime.parse("13:00:00"),
        rotateMember = false,
        error = null,
        enableAddTask = false,
    )
    AddTaskScreen(
        viewState = viewState,
        onBack = { },
        onTaskNameUpdate = { },
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