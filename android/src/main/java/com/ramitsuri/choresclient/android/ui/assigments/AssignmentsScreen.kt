package com.ramitsuri.choresclient.android.ui.assigments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.assignmentHeaderCornerRadius
import com.ramitsuri.choresclient.android.ui.theme.iconWidth
import com.ramitsuri.choresclient.android.ui.theme.marginLarge
import com.ramitsuri.choresclient.android.ui.theme.marginMedium
import com.ramitsuri.choresclient.android.ui.theme.paddingCardView
import com.ramitsuri.choresclient.android.ui.theme.paddingMedium
import com.ramitsuri.choresclient.android.ui.theme.paddingSmall
import com.ramitsuri.choresclient.android.utils.formatRepeatUnit
import com.ramitsuri.choresclient.data.CreateType
import com.ramitsuri.choresclient.data.FilterMode
import com.ramitsuri.choresclient.data.Member
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Task
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AssignmentsScreen(
    modifier: Modifier = Modifier,
    viewModel: AssignmentsViewModel = getViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val viewState = viewModel.state.collectAsState().value
    var selectedAssignmentId by rememberSaveable { mutableStateOf("") }
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetContentColor = MaterialTheme.colorScheme.onBackground,
        sheetContent = {
            // Box is to give something to bottom sheet to draw as initially there would be
            // no sheet content which causes it to error out
            Box(modifier.defaultMinSize(minHeight = 1.dp))
            AssignmentDetailsScreen(
                assignmentId = selectedAssignmentId,
                modalBottomSheetState = modalBottomSheetState,
                markAsDone = { id, progressStatus ->
                    viewModel.changeStateRequested(id, progressStatus)
                }
            )
        }) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = modifier
                .fillMaxSize()
                .displayCutoutPadding(),
        ) { paddingValues ->
            AssignmentsContent(
                isLoading = viewState.loading,
                assignments = viewState.assignments,
                onItemClick = { taskAssignment, clickType ->
                    if (clickType == ClickType.CHANGE_STATUS) {
                        viewModel.changeStateRequested(
                            taskAssignment.id,
                            taskAssignment.progressStatus
                        )
                    } else {
                        selectedAssignmentId = taskAssignment.id
                        coroutineScope.launch {
                            modalBottomSheetState.show()
                        }
                    }
                },
                onRefresh = viewModel::fetchAssignments,
                modifier = modifier.padding(paddingValues),
                selectedFilter = viewState.selectedFilter,
                onFilterSelected = viewModel::filter
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssignmentsContent(
    isLoading: Boolean,
    assignments: Map<String, List<TaskAssignment>>,
    onItemClick: (TaskAssignment, ClickType) -> Unit,
    selectedFilter: FilterMode,
    onFilterSelected: (FilterMode) -> Unit,
    onRefresh: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = paddingMedium)
    ) {
        FilterGroup(selectedFilter = selectedFilter, onFilterSelected = onFilterSelected)
        LoadingContent(
            loading = isLoading,
            empty = assignments.isEmpty(),
            emptyContent = { EmptyContent(onRefresh = { onRefresh(false) }) },
            onRefresh = { onRefresh(false) }) {
            Column(
                modifier = modifier
                    .fillMaxSize()
            ) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(marginMedium)
                ) {
                    assignments.forEach { (header, assignments) ->
                        stickyHeader {
                            AssignmentHeader(text = header)
                        }
                        items(assignments, key = { it.id }) { item ->
                            AssignmentItem(
                                assignment = item,
                                showCompletedButton = true,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    item {
                        Spacer(modifier = modifier.height(marginLarge))
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentHeader(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(vertical = paddingSmall)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(
                        topEnd = assignmentHeaderCornerRadius,
                        bottomEnd = assignmentHeaderCornerRadius
                    )
                )
                .padding(paddingMedium)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignmentItem(
    assignment: TaskAssignment,
    showCompletedButton: Boolean,
    onItemClick: (TaskAssignment, ClickType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {
            onItemClick(assignment, ClickType.DETAIL)
        },
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start, modifier = modifier
                .fillMaxWidth()
                .padding(paddingCardView)
        ) {
            if (showCompletedButton && assignment.progressStatus == ProgressStatus.TODO) {
                Spacer(modifier = modifier.width(marginMedium))
                FilledTonalIconButton(
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    onClick = { onItemClick(assignment, ClickType.CHANGE_STATUS) },
                    modifier = modifier
                        .width(iconWidth)
                        .align(alignment = Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(id = R.string.ok)
                    )
                }
            }
            Spacer(modifier = modifier.width(marginMedium))
            Column(modifier = modifier.weight(1f)) {
                val task = assignment.task
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = modifier.padding(paddingSmall)
                )
                if (task.repeatUnit != RepeatUnit.NONE) {
                    Text(
                        text = formatRepeatUnit(
                            repeatValue = task.repeatValue,
                            repeatUnit = task.repeatUnit
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = modifier.padding(horizontal = paddingSmall)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit
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
        TextButton(onClick = onRefresh) {
            Text(text = stringResource(id = R.string.refresh))
        }
    }
}

@Composable
private fun LoadingContent(
    loading: Boolean,
    empty: Boolean,
    emptyContent: @Composable () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (empty) {
        emptyContent()
    } else {
        SwipeRefresh(
            state = rememberSwipeRefreshState(loading),
            onRefresh = onRefresh,
            modifier = modifier,
            content = content,
        )
    }
}

@Composable
fun FilterGroup(
    selectedFilter: FilterMode,
    onFilterSelected: (FilterMode) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(marginMedium)
    ) {
        item {
            FilterItem(
                text = R.string.assignment_filter_mine,
                contentDescription = R.string.assignment_filter_mine_content_description,
                isSelected = selectedFilter == FilterMode.MINE
            ) {
                onFilterSelected(FilterMode.MINE)
            }
        }
        item {
            FilterItem(
                text = R.string.assignment_filter_other,
                contentDescription = R.string.assignment_filter_other_content_description,
                isSelected = selectedFilter == FilterMode.OTHER
            ) {
                onFilterSelected(FilterMode.OTHER)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterItem(text: Int, contentDescription: Int, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(stringResource(id = text)) },
        selectedIcon = {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = stringResource(id = contentDescription),
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        }
    )
}

@Preview
@Composable
fun PreviewAssignmentContentContentEmpty() {
    ChoresClientTheme {
        Surface {
            AssignmentsContent(
                isLoading = false,
                assignments = mapOf(),
                onItemClick = { taskAssignment: TaskAssignment, clickType: ClickType -> },
                selectedFilter = FilterMode.MINE,
                onFilterSelected = {},
                onRefresh = {}
            )
        }
    }
}

@Preview
@Composable
fun PreviewAssignmentContent() {
    ChoresClientTheme {
        Surface {
            AssignmentsContent(
                isLoading = false,
                assignments = mapOf(
                    "Oct 31" to listOf(
                        TaskAssignment(
                            id = "1",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now(),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now()
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now(),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ),
                        TaskAssignment(
                            id = "2",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now(),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now()
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now(),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ),
                        TaskAssignment(
                            id = "3",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now(),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now()
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now(),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ),
                        TaskAssignment(
                            id = "4",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now(),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now()
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now(),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        )
                    ),
                    "Nov 01" to listOf(
                        TaskAssignment(
                            id = "5",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now(),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now()
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now(),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ),
                        TaskAssignment(
                            id = "6",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now(),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now()
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now(),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ),
                        TaskAssignment(
                            id = "",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "7",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now(),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now()
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now(),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ),
                        TaskAssignment(
                            id = "8",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now(),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now()
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now(),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        )
                    )
                ),
                onRefresh = {},
                onItemClick = { _, _ -> },
                selectedFilter = FilterMode.MINE,
                onFilterSelected = {}
            )
        }
    }
}

@Preview
@Composable
fun PreviewAssignmentHeader() {
    Surface {
        AssignmentHeader(text = "Oct 31")
    }
}

@Preview
@Composable
fun PreviewAssignmentItem() {
    ChoresClientTheme {
        AssignmentItem(
            assignment = TaskAssignment(
                id = "",
                progressStatus = ProgressStatus.TODO,
                progressStatusDate = Clock.System.now(),
                Task(
                    id = "",
                    name = "Clean Kitchen",
                    description = "Clean Kitchen now",
                    dueDateTime = Clock.System.now(),
                    repeatValue = 2,
                    repeatUnit = RepeatUnit.DAY,
                    houseId = "",
                    memberId = "",
                    rotateMember = false,
                    createdDate = Clock.System.now()
                ),
                Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                dueDateTime = Clock.System.now(),
                createdDate = Clock.System.now(),
                createType = CreateType.AUTO
            ),
            showCompletedButton = true,
            { _, _ -> }
        )
    }
}

@Preview
@Composable
fun PreviewFilterItem_Selected() {
    Surface {
        FilterItem(
            text = R.string.assignment_filter_mine,
            contentDescription = R.string.assignment_filter_mine_content_description,
            isSelected = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewFilterItem_NotSelected() {
    Surface {
        FilterItem(
            text = R.string.assignment_filter_mine,
            contentDescription = R.string.assignment_filter_mine_content_description,
            isSelected = false,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewEmptyContent() {
    Surface {
        EmptyContent(onRefresh = {})
    }
}