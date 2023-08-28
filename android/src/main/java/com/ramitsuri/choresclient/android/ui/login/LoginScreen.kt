package com.ramitsuri.choresclient.android.ui.login

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.android.ui.theme.ChoresClientTheme
import com.ramitsuri.choresclient.android.ui.theme.dimens
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.model.DebugButtonAction
import com.ramitsuri.choresclient.model.LoginDebugViewState
import com.ramitsuri.choresclient.model.LoginViewState
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    state: LoginViewState,
    onIdEntered: (String) -> Unit,
    onKeyEntered: (String) -> Unit,
    onLoginClick: () -> Unit,
    onErrorAcknowledged: () -> Unit,
    onServerEntered: (String) -> Unit,
    onServerSet: () -> Unit,
    onResetServer: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.dimens.medium),
        verticalArrangement = Arrangement.Center
    ) {
        if (state.loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LoginContent(
                id = state.id,
                key = state.key,
                enableLogin = state.allowLogin,
                onIdEntered = onIdEntered,
                onKeyEntered = onKeyEntered,
                onLoginClick = onLoginClick
            )
            val debugViewState = state.loginDebugViewState
            if (debugViewState != null) {
                DebugContent(
                    viewState = debugViewState,
                    onServerUrlEntered = onServerEntered,
                    onSetServerRequested = onServerSet,
                    onResetServerRequested = onResetServer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        state.error?.let { error ->
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
            LaunchedEffect(error, snackbarText) {
                snackbarHostState.showSnackbar(snackbarText)
                onErrorAcknowledged()
            }
        }
    }
}

@Composable
private fun LoginContent(
    id: String,
    key: String,
    enableLogin: Boolean,
    onIdEntered: (String) -> Unit,
    onKeyEntered: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    var showPassword by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(focusRequester) {
        delay(100)
        focusRequester.requestFocus()
        keyboard?.show()
    }
    OutlinedTextField(
        value = id,
        singleLine = true,
        onValueChange = { onIdEntered(it) },
        label = { Text(stringResource(id = R.string.login_hint_id)) },
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester = focusRequester)
    )
    Spacer(modifier = Modifier.height(MaterialTheme.dimens.medium))
    OutlinedTextField(
        value = key,
        singleLine = true,
        onValueChange = { onKeyEntered(it) },
        label = { Text(stringResource(id = R.string.login_hint_key)) },
        visualTransformation = if (showPassword) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardActions = KeyboardActions(
            onDone = {
                if (enableLogin) {
                    onLoginClick()
                }
            }),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier.fillMaxWidth(),
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
    Spacer(modifier = Modifier.height(MaterialTheme.dimens.extraLarge))
    FilledTonalButton(
        onClick = onLoginClick,
        enabled = enableLogin,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(stringResource(id = R.string.login_button_login))
    }
}

@Composable
private fun DebugContent(
    viewState: LoginDebugViewState,
    onServerUrlEntered: (String) -> Unit,
    onSetServerRequested: () -> Unit,
    onResetServerRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        SetDebugServerDialog(
            url = viewState.serverUrl,
            onServerUrlEntered = onServerUrlEntered,
            onSetServerRequested = {
                showDialog = !showDialog
                onSetServerRequested()
            },
            onCancelRequested = {
                showDialog = !showDialog
                onResetServerRequested()
            }
        )
    }
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.dimens.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(id = R.string.login_debug_server_url, viewState.serverUrl))
        Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
        Button(
            onClick = {
                when (viewState.debugButtonAction) {
                    DebugButtonAction.RESTART -> {
                        val packageManager = context.packageManager
                        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                        val componentName = intent?.component
                        val mainIntent = Intent.makeRestartActivityTask(componentName)
                        context.startActivity(mainIntent)
                        Runtime.getRuntime().exit(0)
                    }

                    DebugButtonAction.UPDATE_SERVER,
                    DebugButtonAction.SET_SERVER -> {
                        showDialog = true
                    }
                }
            }
        ) {
            when (viewState.debugButtonAction) {
                DebugButtonAction.RESTART -> {
                    Text(stringResource(id = R.string.login_debug_server_restart))
                }

                DebugButtonAction.UPDATE_SERVER -> {
                    Text(stringResource(id = R.string.login_debug_server_update))
                }

                DebugButtonAction.SET_SERVER -> {
                    Text(stringResource(id = R.string.login_debug_server_set))
                }
            }
        }
    }
}

@Composable
private fun SetDebugServerDialog(
    url: String,
    onServerUrlEntered: (String) -> Unit,
    onSetServerRequested: () -> Unit,
    onCancelRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val text by remember(key1 = url) {
        mutableStateOf(
            TextFieldValue(
                url,
                selection = TextRange(url.length)
            )
        )
    }

    LaunchedEffect(focusRequester) {
        delay(100)
        focusRequester.requestFocus()
        keyboard?.show()
    }
    Dialog(
        onDismissRequest = onCancelRequested,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card {
            Column(modifier = modifier.padding(MaterialTheme.dimens.medium)) {
                OutlinedTextField(
                    value = text,
                    singleLine = true,
                    onValueChange = { onServerUrlEntered(it.text) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSetServerRequested()
                        }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester = focusRequester)
                )
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.large))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onSetServerRequested) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    ChoresClientTheme {
        Surface {
            LoginScreen(
                state = LoginViewState(
                    loading = true
                ),
                onIdEntered = { },
                onKeyEntered = { },
                onLoginClick = { },
                onErrorAcknowledged = { },
                onServerEntered = {},
                onServerSet = { },
                onResetServer = { }
            )
        }
    }
}

@Preview
@Composable
private fun EmptyFieldsPreview() {
    ChoresClientTheme {
        Surface {
            LoginScreen(
                state = LoginViewState(
                    loading = false,
                    id = "",
                    key = "",
                ),
                onIdEntered = { },
                onKeyEntered = { },
                onLoginClick = { },
                onErrorAcknowledged = { },
                onServerEntered = {},
                onServerSet = { },
                onResetServer = { }
            )
        }
    }
}

@Preview
@Composable
private fun NonEmptyFieldsPreview() {
    ChoresClientTheme {
        Surface {
            LoginScreen(
                state = LoginViewState(
                    loading = false,
                    id = "id",
                    key = "key",
                ),
                onIdEntered = { },
                onKeyEntered = { },
                onLoginClick = { },
                onErrorAcknowledged = { },
                onServerEntered = {},
                onServerSet = { },
                onResetServer = { }
            )
        }
    }
}

@Preview
@Composable
private fun DebugViewRestartPreview() {
    ChoresClientTheme {
        Surface {
            LoginScreen(
                state = LoginViewState(
                    loading = false,
                    loginDebugViewState = LoginDebugViewState(
                        serverUrl = "http://",
                        debugButtonAction = DebugButtonAction.RESTART
                    )
                ),
                onIdEntered = { },
                onKeyEntered = { },
                onLoginClick = { },
                onErrorAcknowledged = { },
                onServerEntered = {},
                onServerSet = { },
                onResetServer = { }
            )
        }
    }
}

@Preview
@Composable
private fun DebugViewUpdateServerPreview() {
    ChoresClientTheme {
        Surface {
            LoginScreen(
                state = LoginViewState(
                    loading = false,
                    loginDebugViewState = LoginDebugViewState(
                        serverUrl = "http://",
                        debugButtonAction = DebugButtonAction.UPDATE_SERVER
                    )
                ),
                onIdEntered = { },
                onKeyEntered = { },
                onLoginClick = { },
                onErrorAcknowledged = { },
                onServerEntered = {},
                onServerSet = { },
                onResetServer = { }
            )
        }
    }
}

@Preview
@Composable
private fun DebugViewSetServerPreview() {
    ChoresClientTheme {
        Surface {
            LoginScreen(
                state = LoginViewState(
                    loading = false,
                    loginDebugViewState = LoginDebugViewState(
                        serverUrl = "http://",
                        debugButtonAction = DebugButtonAction.SET_SERVER
                    )
                ),
                onIdEntered = { },
                onKeyEntered = { },
                onLoginClick = { },
                onErrorAcknowledged = { },
                onServerEntered = {},
                onServerSet = { },
                onResetServer = { }
            )
        }
    }
}