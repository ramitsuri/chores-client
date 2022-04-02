package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual class DispatcherProvider {
    actual val io: CoroutineDispatcher
        get() = Dispatchers.Default
    actual val default: CoroutineDispatcher
        get() = Dispatchers.Main
    actual val main: CoroutineDispatcher
        get() = Dispatchers.Default

}