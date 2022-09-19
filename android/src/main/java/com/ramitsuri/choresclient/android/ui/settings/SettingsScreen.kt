package com.ramitsuri.choresclient.android.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.preview.FilterPreview
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.marginMedium
import com.ramitsuri.choresclient.android.ui.theme.marginSmall
import com.ramitsuri.choresclient.android.ui.theme.minAssignmentItemHeight
import com.ramitsuri.choresclient.android.ui.theme.paddingCardView
import com.ramitsuri.choresclient.android.ui.theme.paddingLarge
import com.ramitsuri.choresclient.android.ui.theme.paddingMedium
import com.ramitsuri.choresclient.android.ui.theme.paddingSmall
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterItem
import com.ramitsuri.choresclient.model.FilterType
import com.ramitsuri.choresclient.model.FilterViewState
import com.ramitsuri.choresclient.model.SyncViewState
import com.ramitsuri.choresclient.model.TextValue
import com.ramitsuri.choresclient.model.filter.PersonFilterItem
import com.ramitsuri.choresclient.utils.formatSyncTime
import com.ramitsuri.choresclient.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.koin.androidx.compose.getViewModel

//region Main screen

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = getViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit
) {
    val viewState = viewModel.state.collectAsState().value

    SettingsContent(
        snackbarHostState = snackbarHostState,
        error = viewState.error,
        onErrorShown = viewModel::onErrorShown,
        now = Clock.System.now(),
        timeZone = viewState.timeZone,
        syncViewState = viewState.syncViewState,
        onSyncClicked = viewModel::syncRequested,
        filterViewState = viewState.filterViewState,
        onFilterSelected = viewModel::filter,
        onFilterSaveRequested = viewModel::saveFilters,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun SettingsContent(
    snackbarHostState: SnackbarHostState,
    error: ViewError?,
    onErrorShown: () -> Unit,
    now: Instant,
    timeZone: TimeZone,
    syncViewState: SyncViewState,
    onSyncClicked: () -> Unit,
    filterViewState: FilterViewState,
    onFilterSelected: (Filter, FilterItem) -> Unit,
    onFilterSaveRequested: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetContentColor = MaterialTheme.colorScheme.onBackground,
        sheetContent = {
            // Box is to give something to bottom sheet to draw as initially there would be
            // no sheet content which causes it to error out
            Box(modifier.defaultMinSize(minHeight = 1.dp))
            FilterContent(
                filters = filterViewState.filters,
                onItemClick = onFilterSelected
            )
        }) {
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
                    .padding(paddingValues)
                    .padding(horizontal = paddingMedium)
            ) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(marginMedium)
                ) {
                    item {
                        SyncItem(
                            onClick = onSyncClicked,
                            syncViewState = syncViewState,
                            now = now,
                            timeZone = timeZone,
                            modifier = modifier
                        )
                        FilterItem(
                            bottomSheetState = bottomSheetState,
                            filterViewState = filterViewState,
                            onFilterSaveRequested = onFilterSaveRequested
                        )
                    }
                }
            }

            error?.let { error ->
                val snackbarText = when (error) {
                    ViewError.NETWORK ->
                        stringResource(id = R.string.error_network)
                    else ->
                        stringResource(id = R.string.error_unknown)
                }
                LaunchedEffect(error, snackbarText) {
                    snackbarHostState.showSnackbar(snackbarText)
                    onErrorShown()
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minAssignmentItemHeight)
            .clickable(onClick = onClick, enabled = !showProgress)
            .padding(paddingCardView),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(paddingSmall)
        )
        if (showProgress) {
            Spacer(modifier = modifier.height(marginMedium))
            LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        } else {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = modifier.padding(horizontal = paddingSmall)
            )
        }
    }
}

//endregion

//region Sync Item

@Composable
fun SyncItem(
    syncViewState: SyncViewState,
    now: Instant,
    timeZone: TimeZone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsItem(
        title = stringResource(id = R.string.sync),
        subtitle = stringResource(
            id = R.string.settings_item_sync_last_time,
            formatSyncTime(
                toFormat = syncViewState.lastSyncTime,
                now = now,
                timeZone = timeZone
            )
        ),
        onClick = onClick,
        showProgress = syncViewState.loading,
        modifier = modifier
    )
}

//endregion

//region Filter item

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterItem(
    bottomSheetState: ModalBottomSheetState,
    filterViewState: FilterViewState,
    onFilterSaveRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val noneString = stringResource(id = R.string.settings_item_filter_none)
    val personFilter = filterViewState.filters.firstOrNull { it.getType() == FilterType.PERSON }
    val personFilterText = personFilter?.getDisplayText()?.string() ?: "" // Should not be null
    val personFilterSelected = personFilter?.getItems()?.any { it.getIsSelected() } ?: false

    val houseFilter = filterViewState.filters.firstOrNull { it.getType() == FilterType.HOUSE }
    val houseFilterText = houseFilter?.getDisplayText()?.string() ?: "" // Should not be null
    val houseFilterSelected = houseFilter?.getItems()?.any { it.getIsSelected() } ?: false

    val coroutineScope = rememberCoroutineScope()

    SettingsItem(
        title = stringResource(id = R.string.settings_item_default_filter_title),
        subtitle = stringResource(
            id = R.string.settings_item_default_filter,
            if (personFilterSelected) personFilterText else noneString,
            if (houseFilterSelected) houseFilterText else noneString
        ),
        onClick = {
            coroutineScope.launch { bottomSheetState.show() }
        },
        showProgress = false,
        modifier = modifier
    )
    if (bottomSheetState.currentValue != ModalBottomSheetValue.Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                onFilterSaveRequested()
            }
        }
    }

}

@Composable
fun FilterContent(
    filters: List<Filter>,
    onItemClick: (Filter, FilterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingLarge)
    ) {
        LazyColumn {
            items(items = filters, key = { it.getKey() }) { filter ->
                FilterOption(filter = filter, onItemClick = onItemClick, modifier = modifier)
            }
        }
    }
}

@Composable
fun FilterOption(
    filter: Filter,
    onItemClick: (Filter, FilterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when (filter.getType()) {
        FilterType.PERSON ->
            R.string.assignment_filter_person
        FilterType.HOUSE ->
            R.string.assignment_filter_house
    }
    Column(modifier = modifier.padding(paddingMedium)) {
        Text(
            text = stringResource(id = title).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = modifier.padding(paddingMedium),
            color = MaterialTheme.colorScheme.onBackground
        )
        Row {
            filter.getItems().forEach { item ->
                FilterOptionItem(
                    filterType = filter.getType(),
                    item = item,
                    onItemClick = { clickedItem -> onItemClick(filter, clickedItem) },
                    modifier = modifier
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOptionItem(
    filterType: FilterType,
    item: FilterItem,
    onItemClick: (FilterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentDescription = when (filterType) {
        FilterType.PERSON ->
            R.string.assignment_filter_person_content_description
        FilterType.HOUSE ->
            R.string.assignment_filter_house_content_description
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                onItemClick(item)
            }
            .defaultMinSize(minHeight = minAssignmentItemHeight)
            .padding(paddingMedium)) {
        Icon(
            imageVector = if (item.getIsSelected()) {
                Icons.Filled.CheckBox
            } else {
                Icons.Filled.CheckBoxOutlineBlank
            },
            contentDescription = stringResource(id = contentDescription),
            modifier = modifier.size(FilterChipDefaults.IconSize),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = modifier.width(marginSmall))
        Text(
            text = item.getDisplayName().string(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

//endregion

//region Previews

@Preview
@Composable
fun PreviewFilterContent(@PreviewParameter(FilterPreview::class) filters: List<Filter>) {
    ChoresClientTheme {
        Surface {
            FilterContent(filters = filters, onItemClick = { _, _ -> })
        }
    }
}

@Preview
@Composable
fun PreviewFilterContentDark(@PreviewParameter(FilterPreview::class) filters: List<Filter>) {
    ChoresClientTheme(darkTheme = true) {
        Surface {
            FilterContent(filters = filters, onItemClick = { _, _ -> })
        }
    }
}

@Preview
@Composable
fun PreviewFilterOption(@PreviewParameter(FilterPreview::class) filters: List<Filter>) {
    ChoresClientTheme {
        Surface {
            FilterOption(
                filter = filters[0], onItemClick = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
fun PreviewFilterOptionItem() {
    ChoresClientTheme {
        Surface {
            FilterOptionItem(
                filterType = FilterType.PERSON,
                item = PersonFilterItem(
                    id = "1",
                    displayName = TextValue.ForString("Ramit"),
                    selected = true
                ),
                onItemClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSettingsContent(@PreviewParameter(FilterPreview::class) filters: List<Filter>) {
    ChoresClientTheme {
        Surface {
            SettingsContent(
                snackbarHostState = SnackbarHostState(),
                error = null,
                onErrorShown = { },
                now = Clock.System.now(),
                timeZone = TimeZone.currentSystemDefault(),
                syncViewState = SyncViewState(loading = false, lastSyncTime = Clock.System.now()),
                onSyncClicked = { },
                filterViewState = FilterViewState(filters),
                onFilterSelected = { _, _ -> },
                onFilterSaveRequested = {},
                onBack = { }
            )
        }
    }
}

@Preview
@Composable
fun PreviewSettingsItem_Sync() {
    ChoresClientTheme {
        Surface {
            SyncItem(
                onClick = { },
                syncViewState = SyncViewState(
                    loading = false,
                    lastSyncTime = Clock.System.now()
                ),
                now = Clock.System.now(),
                timeZone = TimeZone.currentSystemDefault()
            )
        }
    }
}

@Preview
@Composable
fun PreviewSettingsItem_Sync_WithProgress() {
    ChoresClientTheme {
        Surface {
            SyncItem(
                onClick = { },
                syncViewState = SyncViewState(
                    loading = true,
                    lastSyncTime = Clock.System.now()
                ),
                now = Clock.System.now(),
                timeZone = TimeZone.currentSystemDefault()
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(name = "FilterItem")
@Composable
fun PreviewSettingsItem_Filter(
    @PreviewParameter(FilterPreview::class) filters: List<Filter>
) {
    ChoresClientTheme {
        Surface {
            FilterItem(
                rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
                filterViewState = FilterViewState(filters),
                onFilterSaveRequested = {}
            )
        }
    }
}

//endregion
