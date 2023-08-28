package com.ramitsuri.choresclient.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.preview.FilterPreview
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.model.Filter
import com.ramitsuri.choresclient.model.FilterItem
import com.ramitsuri.choresclient.model.FilterType
import com.ramitsuri.choresclient.model.FilterViewState
import com.ramitsuri.choresclient.model.NotificationActionWrapper
import com.ramitsuri.choresclient.model.NotificationActionsViewState
import com.ramitsuri.choresclient.model.SettingsViewState
import com.ramitsuri.choresclient.model.SyncViewState
import com.ramitsuri.choresclient.utils.formatSyncTime
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    state: SettingsViewState,
    onBack: () -> Unit,
    onSyncClicked: () -> Unit,
    onFilterSelected: (Filter, FilterItem) -> Unit,
    onFilterSaveRequested: () -> Unit,
    onFilterResetRequested: () -> Unit,
    onNotificationActionSelected: (NotificationActionWrapper) -> Unit,
    onNotificationActionsSaveRequested: () -> Unit,
    onNotificationActionsResetRequested: () -> Unit,
    onEnableRemoteLoggingClicked: () -> Unit,
    onErrorAcknowledged: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.dimens.medium)
    ) {
        IconButton(
            onClick = onBack
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.back)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.medium)
        ) {
            item {
                SyncItem(
                    onClick = onSyncClicked,
                    syncViewState = state.syncViewState,
                    timeZone = state.timeZone,
                )
            }
            item {
                FilterItem(
                    filterViewState = state.filterViewState,
                    onFilterSelected = onFilterSelected,
                    onFilterSaveRequested = onFilterSaveRequested,
                    onFilterResetRequested = onFilterResetRequested,
                )
            }
            item {
                NotificationActionItem(
                    viewState = state.notificationActionsViewState,
                    onItemClick = onNotificationActionSelected,
                    onSaveNotificationActionsRequested = onNotificationActionsSaveRequested,
                    onResetNotificationActionsRequested = onNotificationActionsResetRequested,
                )
            }
            item {
                SettingsItemWithSwitch(
                    title = stringResource(id = R.string.settings_remote_logging_title),
                    subtitle = if (state.remoteLoggingEnabled) {
                        stringResource(id = R.string.settings_remote_logging_subtitle_enabled)
                    } else {
                        stringResource(id = R.string.settings_remote_logging_subtitle_disabled)
                    },
                    checked = state.remoteLoggingEnabled,
                    onClick = onEnableRemoteLoggingClicked
                )
            }
            val deviceId = state.deviceId
            if (deviceId != null) {
                item {
                    SettingsItem(
                        title = stringResource(id = R.string.settings_device_id_title),
                        subtitle = deviceId,
                        onClick = { },
                        showProgress = false
                    )
                }
            }
        }

        state.error?.let { error ->
            val snackbarText = when (error) {
                ViewError.NETWORK -> {
                    stringResource(id = R.string.error_network)
                }

                else -> {
                    stringResource(id = R.string.error_unknown)
                }
            }
            LaunchedEffect(error, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                onErrorAcknowledged()
            }
        }
    }
}

@Composable
private fun SyncItem(
    syncViewState: SyncViewState,
    modifier: Modifier = Modifier,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone,
    onClick: () -> Unit,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterItem(
    filterViewState: FilterViewState,
    onFilterSelected: (Filter, FilterItem) -> Unit,
    onFilterSaveRequested: () -> Unit,
    onFilterResetRequested: () -> Unit,
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
    var openBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    SettingsItem(
        title = stringResource(id = R.string.settings_item_default_filter_title),
        subtitle = stringResource(
            id = R.string.settings_item_default_filter,
            if (personFilterSelected) personFilterText else noneString,
            if (houseFilterSelected) houseFilterText else noneString
        ),
        onClick = {
            openBottomSheet = true
        },
        showProgress = false,
        modifier = modifier
    )

    fun hideBottomSheet() {
        coroutineScope.launch {
            bottomSheetState.hide()
        }.invokeOnCompletion {
            if (!bottomSheetState.isVisible) {
                openBottomSheet = false
            }
        }
    }

    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                onFilterResetRequested()
                openBottomSheet = false
            },
        ) {
            FilterContent(
                filters = filterViewState.filters,
                onItemClick = onFilterSelected,
                onSaveFiltersRequested = {
                    onFilterSaveRequested()
                    hideBottomSheet()
                },
                onResetFiltersRequested = {
                    onFilterResetRequested()
                    hideBottomSheet()
                }
            )
        }
    }
}

@Composable
private fun FilterContent(
    filters: List<Filter>,
    onItemClick: (Filter, FilterItem) -> Unit,
    onSaveFiltersRequested: () -> Unit,
    onResetFiltersRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding()
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(items = filters, key = { it.getKey() }) { filter ->
                FilterOption(filter = filter, onItemClick = onItemClick, modifier = Modifier)
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onResetFiltersRequested) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
            TextButton(onClick = onSaveFiltersRequested) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

@Composable
private fun FilterOption(
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
    Column(modifier = modifier.padding(MaterialTheme.dimens.medium)) {
        Text(
            text = stringResource(id = title).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = MaterialTheme.dimens.medium),
            color = MaterialTheme.colorScheme.onBackground
        )
        Row {
            filter.getItems().forEach { item ->
                FilterOptionItem(
                    filterType = filter.getType(),
                    item = item,
                    onItemClick = { clickedItem -> onItemClick(filter, clickedItem) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterOptionItem(
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
            .defaultMinSize(minHeight = MaterialTheme.dimens.minAssignmentItemHeight)
            .padding(MaterialTheme.dimens.medium)) {
        Icon(
            imageVector = if (item.getIsSelected()) {
                Icons.Filled.CheckBox
            } else {
                Icons.Filled.CheckBoxOutlineBlank
            },
            contentDescription = stringResource(id = contentDescription),
            modifier = Modifier.size(FilterChipDefaults.IconSize),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
        Text(
            text = item.getDisplayName().string(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationActionItem(
    viewState: NotificationActionsViewState,
    onItemClick: (NotificationActionWrapper) -> Unit,
    onSaveNotificationActionsRequested: () -> Unit,
    onResetNotificationActionsRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var openBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    SettingsItem(
        title = stringResource(id = R.string.settings_item_notification_action_title),
        subtitle =
        viewState.actions.filter { it.selected }.map { it.name.string() }.joinToString(", "),
        onClick = {
            openBottomSheet = true
        },
        showProgress = false,
        modifier = modifier
    )

    fun hideBottomSheet() {
        coroutineScope.launch {
            bottomSheetState.hide()
        }.invokeOnCompletion {
            if (!bottomSheetState.isVisible) {
                openBottomSheet = false
            }
        }
    }
    if (openBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                onResetNotificationActionsRequested()
                openBottomSheet = false
            },
        ) {
            NotificationActionContent(
                notificationActions = viewState.actions,
                onItemClick = onItemClick,
                onSaveNotificationActionsRequested = {
                    onSaveNotificationActionsRequested()
                    hideBottomSheet()
                },
                onResetNotificationActionsRequested = {
                    onResetNotificationActionsRequested()
                    hideBottomSheet()
                }
            )
        }
    }
}

@Composable
private fun NotificationActionContent(
    notificationActions: List<NotificationActionWrapper>,
    onItemClick: (NotificationActionWrapper) -> Unit,
    onSaveNotificationActionsRequested: () -> Unit,
    onResetNotificationActionsRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.large)
            .systemBarsPadding()
    ) {
        Text(
            text = stringResource(id = R.string.settings_notification_action_select_3),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(MaterialTheme.dimens.medium),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
        LazyColumn {
            items(items = notificationActions, key = { it.action }) { notificationAction ->
                NotificationActionOptionItem(
                    item = notificationAction,
                    onItemClick = onItemClick,
                    modifier = Modifier
                )
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onResetNotificationActionsRequested) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Spacer(modifier = Modifier.width(MaterialTheme.dimens.medium))
            TextButton(onClick = onSaveNotificationActionsRequested) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationActionOptionItem(
    item: NotificationActionWrapper,
    onItemClick: (NotificationActionWrapper) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                onItemClick(item)
            }
            .defaultMinSize(minHeight = MaterialTheme.dimens.minAssignmentItemHeight)
            .padding(MaterialTheme.dimens.medium)) {
        Icon(
            imageVector = if (item.selected) {
                Icons.Filled.CheckBox
            } else {
                Icons.Filled.CheckBoxOutlineBlank
            },
            contentDescription = stringResource(id = R.string.settings_notification_action_content_description),
            modifier = Modifier.size(FilterChipDefaults.IconSize),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(MaterialTheme.dimens.small))
        Text(
            text = item.name.string(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MaterialTheme.dimens.minAssignmentItemHeight)
            .clickable(onClick = onClick, enabled = !showProgress)
            .padding(MaterialTheme.dimens.paddingCardView),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(MaterialTheme.dimens.small)
        )
        if (showProgress) {
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = MaterialTheme.dimens.small)
            )
        }
    }
}

@Composable
private fun SettingsItemWithSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MaterialTheme.dimens.minAssignmentItemHeight)
            .clickable(onClick = onClick)
            .padding(MaterialTheme.dimens.paddingCardView),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(MaterialTheme.dimens.small)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = MaterialTheme.dimens.small)
            )
        }

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
        Switch(
            checked = checked,
            onCheckedChange = { onClick() },
            thumbContent = icon
        )
    }
}

@Preview
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(FilterPreview::class) filters: List<Filter>
) {
    ChoresClientTheme {
        Surface {
            val state = SettingsViewState(
                syncViewState = SyncViewState(
                    loading = false,
                    lastSyncTime = Clock.System.now()
                ),
                filterViewState = FilterViewState(
                    filters = filters
                ),
                notificationActionsViewState = NotificationActionsViewState(
                    actions = listOf()
                ),
                deviceId = null,
                remoteLoggingEnabled = false,
                timeZone = TimeZone.currentSystemDefault(),
                error = null

            )
            SettingsScreen(
                state = state,
                onBack = {},
                onSyncClicked = {},
                onFilterSelected = { _, _ -> },
                onFilterSaveRequested = {},
                onFilterResetRequested = {},
                onNotificationActionSelected = {},
                onNotificationActionsSaveRequested = {},
                onNotificationActionsResetRequested = {},
                onEnableRemoteLoggingClicked = {},
                onErrorAcknowledged = {},
            )
        }
    }
}