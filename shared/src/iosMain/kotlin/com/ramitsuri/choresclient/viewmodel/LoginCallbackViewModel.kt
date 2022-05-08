package com.ramitsuri.choresclient.viewmodel

class LoginCallbackViewModel(vm: LoginViewModel) : CallbackViewModel() {
    override val viewModel: LoginViewModel = vm

    val state = viewModel.state.asCallbacks()

    fun login(id: String, key: String) {
        viewModel.login(id, key)
    }

    fun setDebugServer(server: String) {
        viewModel.setDebugServer(server)
    }

    fun getServer() = viewModel.getServer()
}