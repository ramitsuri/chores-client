package com.ramitsuri.choresclient.android.model

sealed class Result<out T> {
    data class Success<T>(val data: T): Result<T>()
    data class Failure(val error: ViewError): Result<Nothing>()
}

sealed class ViewState<out T> {
    object Loading: ViewState<Nothing>()
    object Reload: ViewState<Nothing>()
    data class Success<T>(val data: T): ViewState<T>()
    data class Error(val error: ViewError): ViewState<Nothing>()
}