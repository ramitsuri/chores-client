package com.ramitsuri.choresclient.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.marginMedium
import com.ramitsuri.choresclient.android.ui.theme.minAssignmentItemHeight
import com.ramitsuri.choresclient.android.ui.theme.paddingCardView
import com.ramitsuri.choresclient.android.ui.theme.paddingMedium
import com.ramitsuri.choresclient.android.ui.theme.paddingSmall
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.utils.formatSyncTime
import com.ramitsuri.choresclient.viewmodel.SettingsViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = getViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit
) {
    val viewState = viewModel.state.collectAsState().value

    SettingsContent(
        isLoading = viewState.loading,
        snackbarHostState = snackbarHostState,
        error = viewState.error,
        onErrorShown = viewModel::onErrorShown,
        lastSyncTime = viewState.lastSyncTime,
        now = Clock.System.now(),
        timeZone = viewState.timeZone,
        onSyncClicked = viewModel::syncRequested,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    error: ViewError?,
    onErrorShown: () -> Unit,
    lastSyncTime: Instant,
    now: Instant,
    timeZone: TimeZone,
    onSyncClicked: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    SettingsItem(
                        title = stringResource(id = R.string.sync),
                        subtitle = stringResource(
                            id = R.string.settings_item_sync_last_time,
                            formatSyncTime(
                                toFormat = lastSyncTime,
                                now = now,
                                timeZone = timeZone
                            )
                        ),
                        onClick = onSyncClicked,
                        showProgress = isLoading
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

@Preview
@Composable
private fun PreviewSettingsContent_Loading() {
    ChoresClientTheme {
        Surface {
            SettingsContent(
                isLoading = true,
                snackbarHostState = SnackbarHostState(),
                error = null,
                onErrorShown = { },
                lastSyncTime = Clock.System.now(),
                now = Clock.System.now(),
                timeZone = TimeZone.currentSystemDefault(),
                onSyncClicked = { },
                onBack = { }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSettingsContent() {
    ChoresClientTheme {
        Surface {
            SettingsContent(
                isLoading = false,
                snackbarHostState = SnackbarHostState(),
                error = null,
                onErrorShown = { },
                lastSyncTime = Clock.System.now(),
                now = Clock.System.now(),
                timeZone = TimeZone.currentSystemDefault(),
                onSyncClicked = { },
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
            SettingsItem(
                title = "Sync",
                subtitle = "Last synced at 10am, Jul 7 2022",
                showProgress = false,
                onClick = { })
        }
    }
}

@Preview
@Composable
fun PreviewSettingsItem_Sync_WithProgress() {
    ChoresClientTheme {
        Surface {
            SettingsItem(
                title = "Sync",
                subtitle = "Last synced at 10am, Jul 7 2022",
                showProgress = true,
                onClick = { })
        }
    }
}
