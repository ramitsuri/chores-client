package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.error.Error

data class LoginViewState(
    val loading: Boolean = false,
    val id: String = "",
    val key: String = "",
    val error: Error? = null,
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