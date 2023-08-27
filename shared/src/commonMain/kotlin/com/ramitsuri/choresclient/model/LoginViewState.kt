package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.ViewError

data class LoginViewState(
    val loading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val id: String = "",
    val key: String = "",
    val error: ViewError? = null,
    val allowLogin: Boolean = false,
    val loginDebugViewState: LoginDebugViewState? = null,
)

data class LoginDebugViewState(
    val serverUrl: String = "",
    val debugButtonAction: DebugButtonAction = DebugButtonAction.UPDATE_SERVER,
)

enum class DebugButtonAction {
    RESTART,
    UPDATE_SERVER,
    SET_SERVER
}