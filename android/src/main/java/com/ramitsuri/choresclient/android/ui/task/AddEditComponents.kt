package com.ramitsuri.choresclient.android.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.ExpandCircleDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.RepeatUnitSelectionItem
import com.ramitsuri.choresclient.model.view.SelectionItem
import com.ramitsuri.choresclient.utils.formatDate
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    now: Instant,
    selectedDate: LocalDate?,
    timeZone: TimeZone,
    onDismissRequested: () -> Unit,
    onDatePicked: (Long) -> Unit
) {
    val initialSelectedDateMillis = selectedDate
        ?.atTime(LocalTime(hour = 0, minute = 0, second = 0))
        ?.toInstant(timeZone)
        ?.toEpochMilliseconds()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val utcDateTime = Instant
                    .fromEpochMilliseconds(utcTimeMillis)
                    .toLocalDateTime(TimeZone.UTC)
                return utcDateTime.date >= now.toLocalDateTime(timeZone).date
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year >= now.toLocalDateTime(timeZone).year
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismissRequested,
        confirmButton = {
            TextButton(onClick = {
                onDismissRequested()
                val selectedDateMillis = datePickerState.selectedDateMillis ?: return@TextButton
                onDatePicked(selectedDateMillis)
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequested) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    ) {
        androidx.compose.material3.DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    time: LocalTime,
    onDismissRequested: () -> Unit,
    onTimePicked: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute)
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
                    text = stringResource(id = R.string.add_task_select_time),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
                androidx.compose.material3.TimePicker(state = state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onDismissRequested
                    ) { Text(stringResource(id = R.string.cancel)) }
                    TextButton(
                        onClick = {
                            onTimePicked(state.hour, state.minute)
                            onDismissRequested()
                        }
                    ) { Text(stringResource(id = R.string.ok)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatInfoDialog(
    repeatValue: Int,
    repeatUnits: List<RepeatUnitSelectionItem>,
    rotateChecked: Boolean,
    now: Instant,
    repeatEndDate: LocalDate?,
    timeZone: TimeZone,
    onRepeatValueUpdated: (String) -> Unit,
    onRepeatUnitSelected: (SelectionItem) -> Unit,
    onRepeatEndDatePicked: (Long) -> Unit,
    onRotateMemberClicked: () -> Unit,
    onResetRepeatInfo: () -> Unit,
    onDismissRequested: () -> Unit,
    focusManager: FocusManager,
) {
    var showDatePickerState by remember { mutableStateOf(false) }
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
                    text = stringResource(id = R.string.add_task_repeat),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = TextFieldValue(
                            text = repeatValue.toString(),
                            selection = TextRange(repeatValue.toString().length)
                        ),
                        singleLine = true,
                        onValueChange = { onRepeatValueUpdated(it.text) },
                        label = { Text(stringResource(id = R.string.add_task_hint_repeat_value)) },
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        }),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.weight(0.4f)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
                    SelectionMenu(
                        startIcon = Icons.Filled.EventRepeat,
                        items = repeatUnits,
                        onItemSelected = {
                            onRepeatUnitSelected(it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
                EndRepetition(
                    enabled = repeatUnits.first { it.getIsSelected() }
                        .toRepeatUnit() != RepeatUnit.NONE,
                    now = now,
                    endDate = repeatEndDate,
                    timeZone = timeZone,
                    onClick = { showDatePickerState = true },
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.small))
                RotateMember(
                    enabled = repeatUnits.first { it.getIsSelected() }
                        .toRepeatUnit() != RepeatUnit.NONE,
                    checked = rotateChecked,
                    onRotateMemberClicked = onRotateMemberClicked
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            onResetRepeatInfo()
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
    if (showDatePickerState) {
        DatePicker(
            now = now,
            selectedDate = repeatEndDate,
            timeZone = timeZone,
            onDismissRequested = { showDatePickerState = false },
            onDatePicked = onRepeatEndDatePicked
        )
    }
}

@Composable
fun EndRepetition(
    enabled: Boolean,
    now: Instant,
    endDate: LocalDate?,
    timeZone: TimeZone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) {
        ContentAlpha.high
    } else {
        ContentAlpha.disabled
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.add_task_end_repetition),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.alpha(alpha)
        )
        PickerItemButton(
            text = if (endDate == null) {
                stringResource(id = R.string.add_task_end_repetition_never)
            } else {
                formatDate(toFormat = endDate, now = now, timeZone = timeZone)
            },
            startIcon = Icons.Filled.CalendarToday,
            onClick = onClick,
            modifier = Modifier
                .padding(MaterialTheme.dimens.small),
            enabled = enabled,
            smallerSize = true
        )

    }
}

@Composable
fun RotateMember(
    enabled: Boolean,
    checked: Boolean,
    onRotateMemberClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: (@Composable () -> Unit)? = if (checked) {
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
    val alpha = if (enabled) {
        ContentAlpha.high
    } else {
        ContentAlpha.disabled
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = Role.Checkbox, onClick = onRotateMemberClicked)
            .padding(MaterialTheme.dimens.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = stringResource(id = R.string.add_task_rotate_member),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.alpha(alpha)
        )
        Spacer(modifier = Modifier.width(MaterialTheme.dimens.large))
        Switch(
            checked = checked,
            onCheckedChange = null,
            thumbContent = icon,
            modifier = Modifier.alpha(alpha)
        )
    }
}

@Composable
fun SelectionMenu(
    startIcon: ImageVector,
    items: List<SelectionItem>,
    onItemSelected: (SelectionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        PickerItemButton(
            text = items.first { it.getIsSelected() }.getDisplayName().string(),
            startIcon = startIcon,
            endIcon = Icons.Filled.ExpandCircleDown,
            onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimens.small)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
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

@Composable
fun PickerItemButton(
    text: String,
    startIcon: ImageVector,
    modifier: Modifier = Modifier,
    endIcon: ImageVector? = null,
    onClick: () -> Unit,
    smallerSize: Boolean = false,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                startIcon,
                contentDescription = text,
                modifier = if (smallerSize) {
                    Modifier.width(MaterialTheme.dimens.iconWidthSmall)
                } else {
                    Modifier
                }
            )
            Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
            Text(
                text = text,
                style = if (smallerSize) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                }
            )
            if (endIcon != null) {
                Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
                Icon(
                    endIcon,
                    contentDescription = text,
                    modifier = if (smallerSize) {
                        Modifier.width(MaterialTheme.dimens.iconWidthSmall)
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}

@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(
        onClick = onBack
    ) {
        Icon(
            Icons.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.back)
        )
    }
}