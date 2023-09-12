package com.ramitsuri.choresclient.model.error

sealed interface Error {

    data class Server(val throwable: Throwable) : Error

    data class NoInternet(val throwable: Throwable) : Error

    data class Unknown(val throwable: Throwable?) : Error
}

sealed interface PushTokenError : Error {
    data object NotLoggedIn: PushTokenError

    data object NoToken: PushTokenError

    data object NoDeviceId: PushTokenError
}

sealed interface EditTaskError: Error {
    data object TaskNotFound: EditTaskError
}
