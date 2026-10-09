package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual class DispatcherProvider {
    actual val io: CoroutineDispatcher
    actual val default: CoroutineDispatcher
    actual val main: CoroutineDispatcher

    actual constructor() {
        this.io = Dispatchers.IO
        this.default = Dispatchers.Default
        this.main = Dispatchers.Main
    }

    actual constructor(
        io: CoroutineDispatcher,
        default: CoroutineDispatcher,
        main: CoroutineDispatcher
    ) {
        this.io = io
        this.default = default
        this.main = main
    }
}