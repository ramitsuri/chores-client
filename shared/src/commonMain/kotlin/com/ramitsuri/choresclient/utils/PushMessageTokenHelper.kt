package com.ramitsuri.choresclient.utils

import kotlinx.coroutines.CoroutineDispatcher

expect suspend fun getToken(
    dispatcher: CoroutineDispatcher,
    logger: LogHelper,
    tag: String
): String?