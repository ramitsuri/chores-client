package com.ramitsuri.choresclient.viewmodel

class LoginCallbackViewModel(vm: LoginViewModel) : CallbackViewModel() {
    override val viewModel: LoginViewModel = vm

    val state = viewModel.state.asCallbacks()

}