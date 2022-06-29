package com.ramitsuri.choresclient.android.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.extensions.getActivity
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.marginExtraLarge
import com.ramitsuri.choresclient.android.ui.theme.marginLarge
import com.ramitsuri.choresclient.android.ui.theme.marginMedium
import com.ramitsuri.choresclient.android.ui.theme.paddingMedium
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.viewmodel.LoginViewModel
import org.koin.androidx.compose.getViewModel
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = getViewModel(),
    onLoggedIn: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val viewState = viewModel.state.collectAsState().value
    if (viewState.isLoggedIn) {
        onLoggedIn()
        return
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        LoginContent(
            isLoading = viewState.loading,
            id = viewState.id,
            idUpdated = viewModel::onIdUpdated,
            key = viewState.key,
            keyUpdated = viewModel::onKeyUpdated,
            onLoginButtonClick = viewModel::login,
            modifier = modifier.padding(paddingValues)
        )
        viewState.loginDebugViewState?.let { debugView ->
            LoginDebugContent(debugView.serverText, viewModel::setDebugServer, modifier)
        }

        viewState.error?.let { error ->
            val snackbarText = when (error) {
                ViewError.NETWORK ->
                    stringResource(id = R.string.error_network)
                ViewError.LOGIN_REQUEST_FAILED ->
                    stringResource(id = R.string.error_login_failed)
                ViewError.LOGIN_NO_TOKEN ->
                    stringResource(id = R.string.error_no_token)
                else ->
                    stringResource(id = R.string.error_unknown)
            }
            LaunchedEffect(viewModel, error, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                viewModel.onErrorShown()
            }
        }
    }
}

@Composable
private fun LoginContent(
    isLoading: Boolean,
    id: String,
    idUpdated: (String) -> Unit,
    key: String,
    keyUpdated: (String) -> Unit,
    onLoginButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingMedium),
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        } else {
            var showPassword by rememberSaveable { mutableStateOf(false) }
            OutlinedTextField(
                value = id,
                singleLine = true,
                onValueChange = { idUpdated(it) },
                label = { Text(stringResource(id = R.string.login_hint_id)) },
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                modifier = modifier.fillMaxWidth()
            )
            Spacer(modifier = modifier.height(marginMedium))
            OutlinedTextField(
                value = key,
                singleLine = true,
                onValueChange = { keyUpdated(it) },
                label = { Text(stringResource(id = R.string.login_hint_key)) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardActions = KeyboardActions(onDone = { onLoginButtonClick() }),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = modifier.fillMaxWidth(),
                trailingIcon = {
                    val icon = if (showPassword) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    }
                    IconButton(onClick = {
                        showPassword = !showPassword
                    }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(id = R.string.login_button_password_visibility_toggle)
                        )
                    }
                }
            )
            Spacer(modifier = modifier.height(marginExtraLarge))
            Button(
                onClick = onLoginButtonClick,
                enabled = id.isNotEmpty() && key.isNotEmpty(),
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.login_button_login))
            }
        }
    }
}

@Composable
private fun LoginDebugContent(
    serverText: String?,
    onServerSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var serverSet by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        SetDebugServerDialog(
            serverText ?: "",
            onPositiveClick = { value ->
                showDialog = !showDialog
                onServerSet(value)
                serverSet = true
            },
            modifier = modifier
        )
    }
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .size(width = 300.dp, height = 300.dp)
    ) {
        Spacer(modifier = modifier.height(100.dp))
        Button(
            onClick = {
                if (serverSet) {
                    val activity = context.getActivity()
                    val intent =
                        activity?.packageManager?.getLaunchIntentForPackage(activity.packageName)
                    activity?.finishAffinity()
                    activity?.startActivity(intent)
                    exitProcess(0)
                } else {
                    showDialog = true
                }
            }
        ) {
            if (serverSet) {
                Text(text = "Restart")
            } else {
                if (serverText.isNullOrEmpty()) {
                    Text("Set Server")
                } else {
                    Text(text = serverText)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetDebugServerDialog(
    previousText: String,
    onPositiveClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf(previousText.ifEmpty { "http://" }) }
    Dialog(onDismissRequest = { }) {
        Card {
            Column(modifier = modifier.padding(paddingMedium)) {
                OutlinedTextField(
                    value = text,
                    singleLine = true,
                    onValueChange = { text = it },
                    modifier = modifier.fillMaxWidth()
                )
                Spacer(modifier = modifier.height(marginLarge))
                Row(horizontalArrangement = Arrangement.End, modifier = modifier.fillMaxWidth()) {
                    TextButton(onClick = {
                        onPositiveClick(text)
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewLoginScreen() {
    ChoresClientTheme {
        Surface {
            LoginContent(isLoading = false, id = "testId", {}, key = "testKey", {}, {})
        }
    }
}

@Preview
@Composable
fun PreviewLoginScreen_IdAndKeyEmpty() {
    ChoresClientTheme {
        Surface {
            LoginContent(isLoading = false, id = "", {}, key = "", {}, {})
        }
    }
}

@Preview
@Composable
fun PreviewLoginScreen_Loading() {
    ChoresClientTheme {
        Surface {
            LoginContent(isLoading = true, id = "", {}, key = "", {}, {})
        }
    }
}

@Preview
@Composable
fun PreviewDebugServerDialog() {
    ChoresClientTheme {
        SetDebugServerDialog(previousText = "Test", onPositiveClick = {})
    }
}

@Preview
@Composable
fun PreviewLoginDebugContent() {
    ChoresClientTheme {
        LoginDebugContent(serverText = "test", onServerSet = {})
    }
}

@Preview
@Composable
fun PreviewLoginDebugContent_ServerTextNull() {
    ChoresClientTheme {
        LoginDebugContent(serverText = null, onServerSet = {})
    }
}