package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.model.error.Error

sealed class Result<out S> {
    data class Success<S>(val data: S) : Result<S>()
    data class Failure(val error: Error) : Result<Nothing>()
}