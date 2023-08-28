package com.ramitsuri.choresclient.android.ui.assigments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.android.utils.formatReminderAt
import com.ramitsuri.choresclient.android.utils.formatRepeatUnit
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.model.AssignmentDetails
import com.ramitsuri.choresclient.utils.now
import com.ramitsuri.choresclient.viewmodel.AssignmentDetailsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AssignmentDetailsScreen(
    assignmentId: String,
    modifier: Modifier = Modifier,
    enableCompleteAndSnooze: Boolean,
    modalBottomSheetState: ModalBottomSheetState,
    viewModel: AssignmentDetailsViewModel = getViewModel(),
    markAsDone: (String, ProgressStatus) -> Unit,
    markAsWontDo: (String, ProgressStatus) -> Unit,
    onSnoozeDay: (String, String) -> Unit,
    onSnoozeHour: (String, String) -> Unit,
    onEditTaskClicked: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val viewState = viewModel.state.collectAsState().value
    LaunchedEffect(key1 = assignmentId, modalBottomSheetState) {
        viewModel.setAssignmentId(assignmentId)
    }
    if (viewState.loading) {
        Loading(modifier)
    } else {
        viewState.assignment?.let {
            AssignmentDetailsContent(
                assignment = it,
                modifier = modifier,
                enableCompleteAndSnooze = enableCompleteAndSnooze,
                onComplete = {
                    markAsDone(assignmentId, ProgressStatus.TODO)
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }
                },
                onWontDo = {
                    markAsWontDo(assignmentId, ProgressStatus.TODO)
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }
                },
                onSnoozeHour = {
                    onSnoozeHour(assignmentId, it.name)
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }
                },
                onSnoozeDay = {
                    onSnoozeDay(assignmentId, it.name)
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }
                },
                onEditRequested = { taskId ->
                    onEditTaskClicked(taskId)
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }
                }
            )
        } ?: run {
            coroutineScope.launch {
                modalBottomSheetState.hide()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailsContent(
    assignment: AssignmentDetails,
    modifier: Modifier = Modifier,
    enableCompleteAndSnooze: Boolean,
    onComplete: () -> Unit,
    onSnoozeHour: () -> Unit,
    onSnoozeDay: () -> Unit,
    onWontDo: () -> Unit,
    onEditRequested: (String) -> Unit
) {
    Column(
        modifier = modifier
            .padding(MaterialTheme.dimens.large)
            .fillMaxWidth()
            .systemBarsPadding()
    ) {
        Text(
            text = assignment.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier.align(CenterHorizontally)
        )
        Spacer(modifier = modifier.height(MaterialTheme.dimens.medium))
        Text(
            text = formatRepeatUnit(
                repeatValue = assignment.repeatValue,
                repeatUnit = assignment.repeatUnit
            ),
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier
                .padding(all = MaterialTheme.dimens.medium)
                .align(CenterHorizontally)
        )
        Text(
            text = assignment.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .padding(all = MaterialTheme.dimens.medium)
        )
        Text(
            text = formatReminderAt(toFormat = assignment.notificationTime),
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .padding(all = MaterialTheme.dimens.medium)
        )
        Text(
            text = stringResource(
                id = R.string.assignment_details_assigned_to_format,
                assignment.member
            ),
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .padding(all = MaterialTheme.dimens.medium)
        )
        Spacer(modifier = modifier.height(MaterialTheme.dimens.large))
        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            FilledTonalButton(
                onClick = onComplete,
                enabled = enableCompleteAndSnooze,
                modifier = modifier.weight(1F)
            ) {
                Text(text = stringResource(id = R.string.assignment_details_button_done))
            }
            Spacer(modifier = modifier.height(MaterialTheme.dimens.large))
            FilledTonalIconButton(
                enabled = enableCompleteAndSnooze,
                onClick = { onEditRequested(assignment.taskId) },
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.edit)
                )
            }
        }
        Spacer(modifier = modifier.height(MaterialTheme.dimens.large))
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
        ) {
            item {
                OutlinedButton(
                    onClick = onSnoozeHour,
                    enabled = enableCompleteAndSnooze
                ) {
                    Text(text = stringResource(id = R.string.assignment_details_button_snooze_hours))
                }
            }
            item {
                OutlinedButton(
                    onClick = onSnoozeDay,
                    enabled = enableCompleteAndSnooze
                ) {
                    Text(text = stringResource(id = R.string.assignment_details_button_snooze_day))
                }
            }
            item {
                OutlinedButton(
                    onClick = onWontDo,
                    enabled = enableCompleteAndSnooze
                ) {
                    Text(text = stringResource(id = R.string.assignment_details_button_wont_do))
                }
            }
        }
        Spacer(modifier = modifier.height(MaterialTheme.dimens.extraLarge))
    }
}

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(MaterialTheme.dimens.large)
            .fillMaxWidth()
            .defaultMinSize(minHeight = MaterialTheme.dimens.minBottomSheetHeight),
        verticalArrangement = Arrangement.Center
    ) {
        LinearProgressIndicator(modifier = modifier.fillMaxWidth())
    }
}

@Preview
@Composable
fun PreviewAssignmentDetailsContent() {
    ChoresClientTheme {
        Surface {
            AssignmentDetailsContent(
                assignment = AssignmentDetails(
                    id = "",
                    taskId = "",
                    name = "Clean kitchen",
                    member = "Paul",
                    description = "Clean kitchen now",
                    repeatValue = 2,
                    repeatUnit = RepeatUnit.DAY,
                    notificationTime = LocalDateTime.now(),
                ),
                enableCompleteAndSnooze = false,
                onComplete = {},
                onSnoozeHour = {},
                onSnoozeDay = {},
                onWontDo = {},
                onEditRequested = {}
            )
        }
    }
}

@Preview
@Composable
fun PreviewLoading() {
    ChoresClientTheme {
        Surface {
            Loading()
        }
    }
}