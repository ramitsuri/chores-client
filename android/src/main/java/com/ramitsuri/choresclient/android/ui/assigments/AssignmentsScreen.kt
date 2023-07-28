package com.ramitsuri.choresclient.android.ui.assigments

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.preview.AssignmentsPreview
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.assignmentHeaderCornerRadius
import com.ramitsuri.choresclient.android.ui.theme.iconWidth
import com.ramitsuri.choresclient.android.ui.theme.marginExtraLarge
import com.ramitsuri.choresclient.android.ui.theme.marginMedium
import com.ramitsuri.choresclient.android.ui.theme.minAssignmentItemHeight
import com.ramitsuri.choresclient.android.ui.theme.paddingCardView
import com.ramitsuri.choresclient.android.ui.theme.paddingMedium
import com.ramitsuri.choresclient.android.ui.theme.paddingSmall
import com.ramitsuri.choresclient.android.utils.formatRepeatUnit
import com.ramitsuri.choresclient.android.utils.observeAsState
import com.ramitsuri.choresclient.data.ActiveStatus
import com.ramitsuri.choresclient.data.CreateType
import com.ramitsuri.choresclient.data.Member
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Task
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.model.AssignmentsMenuItem
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterItem
import com.ramitsuri.choresclient.model.FilterType
import com.ramitsuri.choresclient.model.TaskAssignmentWrapper
import com.ramitsuri.choresclient.model.TextValue
import com.ramitsuri.choresclient.model.filter.PersonFilter
import com.ramitsuri.choresclient.model.filter.PersonFilterItem
import com.ramitsuri.choresclient.utils.now
import com.ramitsuri.choresclient.viewmodel.AssignmentsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AssignmentsScreen(
    shouldRefreshFilter: Boolean,
    modifier: Modifier = Modifier,
    viewModel: AssignmentsViewModel = getViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAddTaskClicked: () -> Unit,
    onEditTaskClicked: (String) -> Unit,
    onSettingsClicked: () -> Unit
) {
    // TODO use shouldRefreshFilter to refresh filters after changed in settings
    val activity = (LocalContext.current as? Activity)
    BackHandler {
        activity?.finish()
    }
    val viewState = viewModel.state.collectAsState().value
    var selectedAssignmentId by rememberSaveable { mutableStateOf("") }
    var enableCompleteAndSnooze by rememberSaveable { mutableStateOf(false) }
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val menu = listOf(AssignmentsMenuItem.SETTINGS)

    val state = LocalLifecycleOwner.current.observeAsState().value
    if (state == Lifecycle.Event.ON_RESUME) {
        // TODO figure out why this is breaking filters and adding multiple items to them
        // viewModel.fetchAssignments(getLocal = true)
    }

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
                    viewModel.markAsDone(id, progressStatus)
                },
                markAsWontDo = { id, progressStatus ->
                    viewModel.markAsWontDo(id, progressStatus)
                },
                onSnoozeDay = { id, assignmentName ->
                    viewModel.onSnoozeDay(id, assignmentName)
                },
                onSnoozeHour = { id, assignmentName ->
                    viewModel.onSnoozeHour(id, assignmentName)
                },
                onEditTaskClicked = onEditTaskClicked,
                enableCompleteAndSnooze = enableCompleteAndSnooze
            )
        }) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTaskClicked,
                    modifier = modifier.navigationBarsPadding()
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
                onItemClick = { taskAssignment, allowEdit, clickType ->
                    if (clickType == ClickType.CHANGE_STATUS) {
                        viewModel.markAsDone(
                            taskAssignment.id,
                            taskAssignment.progressStatus
                        )
                    } else {
                        selectedAssignmentId = taskAssignment.id
                        enableCompleteAndSnooze = allowEdit
                        coroutineScope.launch {
                            modalBottomSheetState.show()
                        }
                    }
                },
                onRefresh = viewModel::fetchAssignments,
                modifier = modifier.padding(paddingValues),
                filters = viewState.filters,
                onFilterSelected = viewModel::filter,
                menu = menu
            ) { menuItem ->
                if (menuItem.id == AssignmentsMenuItem.SETTINGS.id) {
                    onSettingsClicked()
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssignmentsContent(
    isLoading: Boolean,
    assignments: Map<TextValue, List<TaskAssignmentWrapper>>,
    onItemClick: (TaskAssignment, Boolean, ClickType) -> Unit,
    filters: List<Filter>,
    onFilterSelected: (Filter, FilterItem) -> Unit,
    onRefresh: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    menu: List<AssignmentsMenuItem>,
    onMenuSelected: (AssignmentsMenuItem) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = paddingMedium)
    ) {
        FilterRow(
            filters,
            onFilterSelected = onFilterSelected,
            menu = menu,
            onMenuSelected = onMenuSelected,
            modifier = modifier
        )
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
                            AssignmentHeader(text = header.string())
                        }
                        items(assignments, key = { it.assignment.id }) { item ->
                            AssignmentItem(
                                assignment = item.assignment,
                                showCompletedButton = item.enableCompleteButton,
                                onItemClick = onItemClick
                            )
                        }
                    }
                    item {
                        Spacer(modifier = modifier.height(marginExtraLarge))
                        Spacer(modifier = modifier.height(marginExtraLarge))
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
    onItemClick: (TaskAssignment, Boolean, ClickType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {
            onItemClick(assignment, showCompletedButton, ClickType.DETAIL)
        },
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .defaultMinSize(minHeight = minAssignmentItemHeight)
                .padding(paddingCardView)
        ) {
            Spacer(modifier = modifier.width(marginMedium))
            FilledTonalIconButton(
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = { onItemClick(assignment, showCompletedButton, ClickType.CHANGE_STATUS) },
                modifier = modifier
                    .width(iconWidth)
                    .align(alignment = Alignment.CenterVertically),
                enabled = showCompletedButton
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(id = R.string.ok)
                )
            }
            Spacer(modifier = modifier.width(marginMedium))
            Column(
                modifier = modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val task = assignment.task
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = modifier.padding(paddingSmall)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assignment.member.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = modifier
                            .background(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            )
                            .padding(paddingSmall)
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
fun FilterRow(
    filters: List<Filter>,
    onFilterSelected: (Filter, FilterItem) -> Unit,
    menu: List<AssignmentsMenuItem>,
    onMenuSelected: (AssignmentsMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(marginMedium),
            modifier = modifier.weight(1F)
        ) {
            items(filters, key = { it.getKey() }) { item ->
                FilterOption(filter = item, onFilterSelected = onFilterSelected)
            }
        }
        MoreMenu(menu, onMenuSelected, modifier)
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
    Box {
        IconButton(
            onClick = {
                expanded = !expanded
            },
            modifier = modifier
                .size(iconWidth)
                .padding(paddingSmall)
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
        imageVector = Icons.Filled.List,
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
                assignments = mapOf(),
                onItemClick = { _: TaskAssignment, _: Boolean, _: ClickType -> },
                filters = listOf(),
                onFilterSelected = { _, _ -> },
                onRefresh = {},
                menu = listOf(AssignmentsMenuItem.SETTINGS)
            ) {

            }
        }
    }
}

@Preview
@Composable
fun PreviewAssignmentContent_personFilter(
    @PreviewParameter(AssignmentsPreview::class) assignments: Map<TextValue, List<TaskAssignmentWrapper>>
) {
    ChoresClientTheme {
        Surface {
            AssignmentsContent(
                isLoading = false,
                assignments = assignments,
                onItemClick = { _, _, _ -> },
                filters = listOf(
                    PersonFilter(
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
                ),
                onFilterSelected = { _, _ -> },
                onRefresh = {},
                menu = listOf(AssignmentsMenuItem.SETTINGS),
            ) {

            }
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
                    dueDateTime = LocalDateTime.now(),
                    repeatValue = 2,
                    repeatUnit = RepeatUnit.DAY,
                    houseId = "",
                    memberId = "",
                    rotateMember = false,
                    createdDate = Clock.System.now(),
                    status = ActiveStatus.ACTIVE
                ),
                Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                dueDateTime = LocalDateTime.now(),
                createdDate = Clock.System.now(),
                createType = CreateType.AUTO
            ),
            showCompletedButton = true,
            { _, _, _ -> }
        )
    }
}

@Preview
@Composable
fun PreviewAssignmentItem_completeButtonDisabled() {
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
                    dueDateTime = LocalDateTime.now(),
                    repeatValue = 2,
                    repeatUnit = RepeatUnit.DAY,
                    houseId = "",
                    memberId = "",
                    rotateMember = false,
                    createdDate = Clock.System.now(),
                    status = ActiveStatus.ACTIVE
                ),
                Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                dueDateTime = LocalDateTime.now(),
                createdDate = Clock.System.now(),
                createType = CreateType.AUTO
            ),
            showCompletedButton = false,
            { _, _, _ -> }
        )
    }
}

@Preview
@Composable
fun PreviewAssignmentItemNoRepeat() {
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
                    dueDateTime = LocalDateTime.now(),
                    repeatValue = 0,
                    repeatUnit = RepeatUnit.NONE,
                    houseId = "",
                    memberId = "",
                    rotateMember = false,
                    createdDate = Clock.System.now(),
                    status = ActiveStatus.ACTIVE
                ),
                Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                dueDateTime = LocalDateTime.now(),
                createdDate = Clock.System.now(),
                createType = CreateType.AUTO
            ),
            showCompletedButton = true,
            { _, _, _ -> }
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
