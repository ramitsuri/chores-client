package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.CoroutineDispatcher

expect class DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher

    constructor()
    constructor(
        io: CoroutineDispatcher,
        default: CoroutineDispatcher,
        main: CoroutineDispatcher
    )
}