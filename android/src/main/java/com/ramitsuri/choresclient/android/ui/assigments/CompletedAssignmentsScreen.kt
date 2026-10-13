package com.ramitsuri.choresclient.android.ui.assigments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.common.AppTopAppBar
import com.ramitsuri.choresclient.android.ui.common.ListTypeSelection
import com.ramitsuri.choresclient.android.ui.common.PersonalSharedToggle
import com.ramitsuri.choresclient.android.ui.common.TaskCard
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.model.entities.RepeatInfo
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.view.CompletedAssignmentsViewState
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.utils.now
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock

@Composable
fun CompletedAssignmentsScreen(
    viewState: CompletedAssignmentsViewState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedListType by remember { mutableStateOf(ListTypeSelection.PERSONAL) }
    val isShared = selectedListType == ListTypeSelection.SHARED
    val displayAssignments = viewState.assignments.filter { it.isSharedList == isShared }

    Scaffold(
        topBar = {
            AppTopAppBar(
                onBack = onBack
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
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
        ) {
            PersonalSharedToggle(
                selectedType = selectedListType,
                onTypeSelected = { selectedListType = it }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))

            if (viewState.loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } else if (displayAssignments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.completed_empty),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
                ) {
                    items(
                        items = displayAssignments,
                        key = { it.taskAssignment.id }
                    ) { details ->
                        TaskCard(
                            details = details,
                            isCompleted = true
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(MaterialTheme.dimens.extraLarge))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCompletedAssignmentsScreen() {
    ChoresClientTheme {
        Surface {
            CompletedAssignmentsScreen(
                viewState = CompletedAssignmentsViewState(
                    loading = false,
                    assignments = listOf(
                        TaskAssignmentDetails(
                            taskAssignment = TaskAssignment(
                                id = "1",
                                progressStatus = ProgressStatus.DONE,
                                progressStatusDate = Clock.System.now(),
                                taskId = "taskId",
                                taskName = "Clean Kitchen",
                                houseId = "houseId",
                                repeatInfo = RepeatInfo(
                                    repeatValue = 1,
                                    repeatUnit = RepeatUnit.DAY,
                                    repeatEndDateTime = null
                                ),
                                memberId = "memberId",
                                memberName = "You",
                                dueDateTime = LocalDateTime.now()
                            ),
                            reminderTime = null,
                            enableSnooze = false,
                            willReminderBeSet = false,
                            assignedToLoggedInUser = true,
                        )
                    ),
                ),
                onBack = {},
            )
        }
    }
}
