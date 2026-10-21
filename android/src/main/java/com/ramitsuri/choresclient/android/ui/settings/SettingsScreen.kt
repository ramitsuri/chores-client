package com.ramitsuri.choresclient.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.string
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.model.error.EditTaskError
import com.ramitsuri.choresclient.model.error.Error
import com.ramitsuri.choresclient.model.error.PushTokenError
import com.ramitsuri.choresclient.model.view.NotificationActionWrapper
import com.ramitsuri.choresclient.model.view.NotificationActionsViewState
import com.ramitsuri.choresclient.model.view.SettingsViewState
import com.ramitsuri.choresclient.model.view.SyncViewState
import com.ramitsuri.choresclient.utils.formatSyncTime
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    state: SettingsViewState,
    onBack: () -> Unit,
    onSyncClicked: () -> Unit,
    onNotificationActionSelected: (NotificationActionWrapper) -> Unit,
    onNotificationActionsSaveRequested: () -> Unit,
    onNotificationActionsResetRequested: () -> Unit,
    onEnableRemoteLoggingClicked: () -> Unit,
    onAllowTaskAssignmentUndoClicked: () -> Unit,
    onSwipeForTaskAssignmentCompletionClicked: () -> Unit,
    onEnableRemindPastDueClicked: () -> Unit,
    onErrorAcknowledged: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Surface {
        Column(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPadding()
                .displayCutoutPadding(),
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
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
                    SettingsItemWithSwitch(
                        title = stringResource(id = R.string.settings_remind_past_due_title),
                        subtitle = if (state.remindPastDueEnabled) {
                            stringResource(id = R.string.settings_remind_past_due_subtitle_enabled)
                        } else {
                            stringResource(id = R.string.settings_remind_past_due_subtitle_disabled)
                        },
                        checked = state.remindPastDueEnabled,
                        onClick = onEnableRemindPastDueClicked
                    )
                }
                item {
                    SettingsItemWithSwitch(
                        title = stringResource(id = R.string.settings_allow_task_assignment_undo),
                        subtitle = null,
                        checked = state.allowTaskAssignmentUndo,
                        onClick = onAllowTaskAssignmentUndoClicked,
                    )
                }
                item {
                    SettingsItemWithSwitch(
                        title = stringResource(id = R.string.settings_swipe_for_task_assignment_completion),
                        subtitle = null,
                        checked = state.swipeForTaskAssignmentCompletion,
                        onClick = onSwipeForTaskAssignmentCompletionClicked,
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
                    is Error.NoInternet -> stringResource(id = R.string.error_network)
                    is PushTokenError.NoDeviceId,
                    is PushTokenError.NoToken,
                    is PushTokenError.NotLoggedIn,
                    is Error.Server,
                    is Error.Unknown -> {
                        stringResource(id = R.string.error_unknown)
                    }

                    EditTaskError.TaskNotFound -> {
                        // Not possible here
                        return@let
                    }
                }
                LaunchedEffect(error, snackbarText) {
                    snackbarHostState.showSnackbar(snackbarText)
                    onErrorAcknowledged()
                }
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
    subtitle: String?,
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
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
            subtitle?.let {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = MaterialTheme.dimens.small)
                )
            }
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
private fun SettingsScreenPreview() {
    ChoresClientTheme {
        Surface {
            val state = SettingsViewState(
                syncViewState = SyncViewState(
                    loading = false,
                    lastSyncTime = Clock.System.now()
                ),
                notificationActionsViewState = NotificationActionsViewState(
                    actions = listOf()
                ),
                deviceId = null,
                remoteLoggingEnabled = false,
                remindPastDueEnabled = false,
                allowTaskAssignmentUndo = false,
                swipeForTaskAssignmentCompletion = true,
                timeZone = TimeZone.currentSystemDefault(),
                error = null
            )
            SettingsScreen(
                state = state,
                onBack = {},
                onSyncClicked = {},
                onNotificationActionSelected = {},
                onNotificationActionsSaveRequested = {},
                onNotificationActionsResetRequested = {},
                onEnableRemoteLoggingClicked = {},
                onAllowTaskAssignmentUndoClicked = {},
                onSwipeForTaskAssignmentCompletionClicked = {},
                onErrorAcknowledged = {},
                onEnableRemindPastDueClicked = {},
            )
        }
    }
}
