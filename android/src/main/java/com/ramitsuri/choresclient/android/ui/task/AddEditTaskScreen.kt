package com.ramitsuri.choresclient.android.ui.task

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.model.HouseSelectionItem
import com.ramitsuri.choresclient.model.MemberSelectionItem
import com.ramitsuri.choresclient.model.RepeatUnitSelectionItem
import com.ramitsuri.choresclient.model.SelectionItem
import com.ramitsuri.choresclient.utils.formatPickedDate
import com.ramitsuri.choresclient.utils.formatPickedTime
import com.ramitsuri.choresclient.utils.now
import com.ramitsuri.choresclient.viewmodel.AddEditTaskViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalTime
import org.koin.androidx.compose.getViewModel

@Composable
fun AddEditTasksScreen(
    modifier: Modifier = Modifier,
    taskId: String?,
    viewModel: AddEditTaskViewModel = getViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }
    val viewState = viewModel.state.collectAsState().value

    if (viewState.taskSaved) {
        onBack()
        return
    }
    LaunchedEffect(key1 = taskId) {
        viewModel.setTaskId(taskId)
    }

    AddEditTaskContent(
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        isLoading = viewState.loading,
        taskName = viewState.taskName,
        taskNameUpdated = viewModel::onTaskNameUpdated,
        taskDescription = viewState.taskDescription,
        taskDescriptionUpdated = viewModel::onTaskDescriptionUpdated,
        repeatValue = viewState.repeatValue,
        repeatValueUpdated = viewModel::onRepeatValueUpdated,
        repeatUnits = viewState.repeatUnits,
        repeatUnitSelected = viewModel::onRepeatUnitSelected,
        date = viewState.date,
        isDatePicked = viewState.isDatePicked,
        dateUpdated = viewModel::onDatePicked,
        time = viewState.time,
        isTimePicked = viewState.isTimePicked,
        timeUpdated = viewModel::onTimePicked,
        houses = viewState.houses,
        houseSelected = viewModel::onHouseSelected,
        members = viewState.members,
        memberSelected = viewModel::onMemberSelected,
        rotateMember = viewState.rotateMember,
        rotateMemberUpdated = viewModel::onRotateMemberUpdated,
        onAddTaskRequested = viewModel::saveTask,
        canAddTask = viewState.enableSaveTask,
        modifier = modifier
    )
    viewState.error?.let { error ->
        val snackbarText = when (error) {
            ViewError.ADD_TASK_ERROR ->
                stringResource(id = R.string.error_add_task)
            ViewError.EDIT_TASK_ERROR ->
                stringResource(id = R.string.error_edit_task)
            else ->
                stringResource(id = R.string.error_unknown)
        }
        LaunchedEffect(viewModel, error, snackbarText) {
            snackbarHostState.showSnackbar(snackbarText)
            viewModel.onErrorShown()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskContent(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    isLoading: Boolean,
    taskName: String,
    taskNameUpdated: (String) -> Unit,
    taskDescription: String,
    taskDescriptionUpdated: (String) -> Unit,
    repeatValue: Int,
    repeatValueUpdated: (String) -> Unit,
    repeatUnits: List<RepeatUnitSelectionItem>,
    repeatUnitSelected: (String) -> Unit,
    houses: List<HouseSelectionItem>,
    houseSelected: (String) -> Unit,
    members: List<MemberSelectionItem>,
    memberSelected: (String) -> Unit,
    date: LocalDate,
    isDatePicked: Boolean,
    dateUpdated: (LocalDate) -> Unit,
    time: LocalTime,
    isTimePicked: Boolean,
    timeUpdated: (LocalTime) -> Unit,
    rotateMember: Boolean,
    rotateMemberUpdated: (Boolean) -> Unit,
    onAddTaskRequested: () -> Unit,
    canAddTask: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .displayCutoutPadding(),
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(MaterialTheme.dimens.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = modifier.fillMaxWidth())
            } else {
                Name(
                    taskName = taskName,
                    taskNameUpdated = taskNameUpdated,
                    focusManager = focusManager,
                    modifier = modifier
                )

                Spacer(modifier = modifier.height(MaterialTheme.dimens.medium))

                Description(
                    taskDescription = taskDescription,
                    taskDescriptionUpdated = taskDescriptionUpdated,
                    focusManager = focusManager,
                    modifier = modifier
                )

                Spacer(modifier = modifier.height(MaterialTheme.dimens.medium))

                Repeat(
                    repeatValue = repeatValue,
                    repeatValueUpdated = repeatValueUpdated,
                    repeatUnits = repeatUnits,
                    repeatUnitSelected = repeatUnitSelected,
                    focusManager = focusManager,
                    modifier = modifier
                )

                Spacer(modifier = modifier.height(MaterialTheme.dimens.medium))

                DateTime(
                    date = date,
                    isDatePicked = isDatePicked,
                    dateUpdated = dateUpdated,
                    time = time,
                    isTimePicked = isTimePicked,
                    timeUpdated = timeUpdated,
                    modifier = modifier
                )

                Spacer(modifier = modifier.height(MaterialTheme.dimens.medium))

                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionMenu(
                        items = houses,
                        onItemSelected = {
                            houseSelected(it.getId())
                        },
                        noSelectionResId = R.string.add_task_select_house,
                        modifier = modifier
                            .weight(1.5f)
                    )

                    Spacer(modifier = modifier.width(MaterialTheme.dimens.medium))

                    SelectionMenu(
                        items = members,
                        onItemSelected = {
                            memberSelected(it.getId())
                        },
                        noSelectionResId = R.string.add_task_select_member,
                        modifier = modifier
                            .weight(1f)
                    )
                }

                Spacer(modifier = modifier.height(MaterialTheme.dimens.medium))

                RotateMember(
                    rotateMember = rotateMember,
                    rotateMemberUpdated = rotateMemberUpdated
                )

                Spacer(modifier = modifier.height(MaterialTheme.dimens.extraLarge))

                FilledTonalButton(
                    onClick = onAddTaskRequested,
                    modifier = modifier.fillMaxWidth(),
                    enabled = canAddTask
                ) {
                    Text(text = stringResource(id = R.string.add_task_add))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Name(
    taskName: String,
    taskNameUpdated: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = taskName,
        singleLine = true,
        onValueChange = { taskNameUpdated(it) },
        label = { Text(stringResource(id = R.string.add_task_hint_name)) },
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Description(
    taskDescription: String,
    taskDescriptionUpdated: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = taskDescription,
        singleLine = true,
        onValueChange = { taskDescriptionUpdated(it) },
        label = { Text(stringResource(id = R.string.add_task_hint_description)) },
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DateTime(
    date: LocalDate,
    isDatePicked: Boolean,
    dateUpdated: (LocalDate) -> Unit,
    time: LocalTime,
    isTimePicked: Boolean,
    timeUpdated: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateDialogState = rememberMaterialDialogState()
    val timeDialogState = rememberMaterialDialogState()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TextButton(
            onClick = {
                dateDialogState.show()
            },
            modifier = modifier
                .weight(1f)
        ) {
            Text(
                text = if (isDatePicked) {
                    formatPickedDate(date)
                } else {
                    stringResource(id = R.string.add_task_pick_date)
                }
            )
        }

        Spacer(modifier = modifier.width(MaterialTheme.dimens.medium))

        TextButton(
            onClick = {
                timeDialogState.show()
            },
            modifier = modifier
                .weight(1f)
        ) {
            Text(
                text = if (isTimePicked) {
                    formatPickedTime(time)
                } else {
                    stringResource(id = R.string.add_task_pick_time)
                }
            )
        }
    }
    DatePicker(
        dialogState = dateDialogState,
        initialDate = date,
        onDatePicked = dateUpdated,
        modifier = modifier
    )
    TimePicker(
        dialogState = timeDialogState,
        initialTime = time,
        onTimePicked = timeUpdated,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Repeat(
    repeatValue: Int,
    repeatValueUpdated: (String) -> Unit,
    repeatUnits: List<RepeatUnitSelectionItem>,
    repeatUnitSelected: (String) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = TextFieldValue(
                repeatValue.toString(),
                selection = TextRange(repeatValue.toString().length)
            ),
            singleLine = true,
            onValueChange = { repeatValueUpdated(it.text) },
            label = { Text(stringResource(id = R.string.add_task_hint_repeat_value)) },
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = modifier
                .weight(1f)
        )

        Spacer(modifier = modifier.width(MaterialTheme.dimens.medium))

        SelectionMenu(
            items = repeatUnits,
            onItemSelected = {
                repeatUnitSelected(it.getId())
            },
            noSelectionResId = R.string.add_task_select_repeat_unit,
            modifier = modifier
                .weight(2f)
        )
    }
}

@Composable
fun RotateMember(
    modifier: Modifier = Modifier,
    rotateMember: Boolean,
    rotateMemberUpdated: (Boolean) -> Unit
) {
    val icon: (@Composable () -> Unit)? = if (rotateMember) {
        {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    } else {
        null
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.add_task_rotate_member),
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = modifier.width(MaterialTheme.dimens.large))
        Switch(
            checked = rotateMember,
            onCheckedChange = rotateMemberUpdated,
            thumbContent = icon
        )
    }
}

@Composable
fun DatePicker(
    dialogState: MaterialDialogState,
    initialDate: LocalDate,
    onDatePicked: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialDateJvm = initialDate.toJavaLocalDate()
    var pickedDate by remember {
        mutableStateOf(initialDate.toJavaLocalDate())
    }
    MaterialDialog(
        dialogState = dialogState,
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        datepicker(
            initialDate = initialDateJvm,
            title = stringResource(id = R.string.add_task_pick_date),
            yearRange = initialDateJvm.year..initialDateJvm.year + 5,
            colors = DatePickerDefaults.colors(
                headerBackgroundColor = MaterialTheme.colorScheme.background,
                headerTextColor = MaterialTheme.colorScheme.primary,
                calendarHeaderTextColor = MaterialTheme.colorScheme.primary,
                dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
                dateInactiveBackgroundColor = MaterialTheme.colorScheme.surface,
                dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
                dateInactiveTextColor = MaterialTheme.colorScheme.onSurface
            ),
            waitForPositiveButton = false,
            allowedDateValidator = {
                it >= initialDateJvm
            }
        ) {
            pickedDate = it
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.dimens.large),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                this@MaterialDialog.dialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Spacer(modifier = modifier.width(MaterialTheme.dimens.medium))
            TextButton(onClick = {
                onDatePicked(pickedDate.toKotlinLocalDate())
                this@MaterialDialog.dialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

@Composable
fun TimePicker(
    dialogState: MaterialDialogState,
    initialTime: LocalTime,
    onTimePicked: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialTimeJvm = initialTime.toJavaLocalTime()
    var pickedTime by remember {
        mutableStateOf(initialTime.toJavaLocalTime())
    }
    MaterialDialog(
        dialogState = dialogState,
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        timepicker(
            initialTime = initialTimeJvm,
            title = stringResource(id = R.string.add_task_pick_time),
            colors = TimePickerDefaults.colors(
                activeBackgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                headerTextColor = MaterialTheme.colorScheme.primary,
                inactiveBackgroundColor = MaterialTheme.colorScheme.background,
                activeTextColor = MaterialTheme.colorScheme.primary,
                inactiveTextColor = MaterialTheme.colorScheme.onBackground,
                inactivePeriodBackground = MaterialTheme.colorScheme.background,
                selectorColor = MaterialTheme.colorScheme.primary,
                selectorTextColor = MaterialTheme.colorScheme.primary,
                borderColor = MaterialTheme.colorScheme.onPrimary,
            ),
            waitForPositiveButton = false
        ) {
            pickedTime = it
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.dimens.large),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = {
                this@MaterialDialog.dialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Spacer(modifier = modifier.width(MaterialTheme.dimens.medium))
            TextButton(onClick = {
                onTimePicked(pickedTime.toKotlinLocalTime())
                this@MaterialDialog.dialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

@Composable
fun SelectionMenu(
    items: List<SelectionItem>,
    onItemSelected: (SelectionItem) -> Unit,
    @StringRes noSelectionResId: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        TextButton(
            onClick = {
                expanded = !expanded
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.small)
        ) {
            Text(
                text = items.firstOrNull { it.getIsSelected() }?.getDisplayName()?.string()
                    ?: stringResource(id = noSelectionResId)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.getDisplayName().string()) },
                    onClick = {
                        expanded = false
                        onItemSelected(item)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewAddTaskContent() {
    ChoresClientTheme {
        Surface {
            AddEditTaskContent(
                snackbarHostState = SnackbarHostState(),
                onBack = {},
                isLoading = false,
                taskName = "",
                taskNameUpdated = {},
                taskDescription = "",
                taskDescriptionUpdated = {},
                repeatValue = 0,
                repeatValueUpdated = {},
                repeatUnits = RepeatUnit.values().toList()
                    .map { RepeatUnitSelectionItem(it, false) },
                repeatUnitSelected = {},
                date = LocalDateTime.now().date,
                isDatePicked = false,
                dateUpdated = {},
                time = LocalDateTime.now().time,
                isTimePicked = false,
                timeUpdated = {},
                houses = listOf(),
                houseSelected = {},
                members = listOf(),
                onAddTaskRequested = {},
                memberSelected = {},
                rotateMember = false,
                rotateMemberUpdated = {},
                canAddTask = true
            )
        }
    }
}

@Preview
@Composable
fun PreviewAddTaskContent_loading() {
    ChoresClientTheme {
        Surface {
            AddEditTaskContent(
                snackbarHostState = SnackbarHostState(),
                onBack = {},
                isLoading = true,
                taskName = "",
                taskNameUpdated = {},
                taskDescription = "",
                taskDescriptionUpdated = {},
                repeatValue = 0,
                repeatValueUpdated = {},
                repeatUnits = RepeatUnit.values().toList()
                    .map { RepeatUnitSelectionItem(it, false) },
                repeatUnitSelected = {},
                date = LocalDateTime.now().date,
                isDatePicked = false,
                dateUpdated = {},
                time = LocalDateTime.now().time,
                isTimePicked = false,
                timeUpdated = {},
                houses = listOf(),
                houseSelected = {},
                members = listOf(),
                onAddTaskRequested = {},
                memberSelected = {},
                rotateMember = false,
                rotateMemberUpdated = {},
                canAddTask = true
            )
        }
    }
}