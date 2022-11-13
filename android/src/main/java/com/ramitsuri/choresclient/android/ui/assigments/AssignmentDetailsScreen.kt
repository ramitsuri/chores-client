package com.ramitsuri.choresclient.android.ui.assigments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material3.FilledTonalButton
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
import com.ramitsuri.choresclient.android.ui.theme.marginExtraLarge
import com.ramitsuri.choresclient.android.ui.theme.marginLarge
import com.ramitsuri.choresclient.android.ui.theme.marginMedium
import com.ramitsuri.choresclient.android.ui.theme.minBottomSheetHeight
import com.ramitsuri.choresclient.android.ui.theme.paddingLarge
import com.ramitsuri.choresclient.android.ui.theme.paddingMedium
import com.ramitsuri.choresclient.android.utils.formatReminderAt
import com.ramitsuri.choresclient.android.utils.formatRepeatUnit
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.model.AssignmentDetails
import com.ramitsuri.choresclient.viewmodel.AssignmentDetailsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AssignmentDetailsScreen(
    assignmentId: String,
    modifier: Modifier = Modifier,
    enableCompleteAndSnooze: Boolean,
    modalBottomSheetState: ModalBottomSheetState,
    viewModel: AssignmentDetailsViewModel = getViewModel(),
    markAsDone: (String, ProgressStatus) -> Unit
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
                    coroutineScope.launch {
                        markAsDone(assignmentId, ProgressStatus.TODO)
                        modalBottomSheetState.hide()
                    }
                },
                onSnoozeHour = {
                    viewModel.onSnoozeHour()
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }
                },
                onSnoozeDay = {
                    viewModel.onSnoozeDay()
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                    }
                },
            )
        } ?: run {
            coroutineScope.launch {
                modalBottomSheetState.hide()
            }
        }
    }
}

@Composable
fun AssignmentDetailsContent(
    assignment: AssignmentDetails,
    modifier: Modifier = Modifier,
    enableCompleteAndSnooze: Boolean,
    onComplete: () -> Unit,
    onSnoozeHour: () -> Unit,
    onSnoozeDay: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(paddingLarge)
            .fillMaxWidth()
    ) {
        Text(
            text = assignment.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = modifier.align(CenterHorizontally)
        )
        Spacer(modifier = modifier.height(marginMedium))
        Text(
            text = formatRepeatUnit(
                repeatValue = assignment.repeatValue,
                repeatUnit = assignment.repeatUnit
            ),
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier
                .padding(all = paddingMedium)
                .align(CenterHorizontally)
        )
        Text(
            text = assignment.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .padding(all = paddingMedium)
        )
        Text(
            text = formatReminderAt(toFormat = assignment.notificationTime),
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .padding(all = paddingMedium)
        )
        Spacer(modifier = modifier.height(marginLarge))
        FilledTonalButton(
            onClick = onComplete,
            enabled = enableCompleteAndSnooze,
            modifier = modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.assignment_details_button_done))
        }
        Spacer(modifier = modifier.height(marginLarge))
        Row(modifier = modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onSnoozeHour,
                enabled = enableCompleteAndSnooze
            ) {
                Text(text = stringResource(id = R.string.assignment_details_button_snooze_hours))
            }
            Spacer(modifier = modifier.width(marginMedium))
            OutlinedButton(
                onClick = onSnoozeDay,
                enabled = enableCompleteAndSnooze
            ) {
                Text(text = stringResource(id = R.string.assignment_details_button_snooze_day))
            }
        }
        Spacer(modifier = modifier.height(marginExtraLarge))
    }
}

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(paddingLarge)
            .fillMaxWidth()
            .defaultMinSize(minHeight = minBottomSheetHeight),
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
                    name = "Clean kitchen",
                    description = "Clean kitchen now",
                    repeatValue = 2,
                    repeatUnit = RepeatUnit.DAY,
                    notificationTime = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()),
                ),
                onComplete = {},
                onSnoozeHour = {},
                onSnoozeDay = {},
                enableCompleteAndSnooze = false
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