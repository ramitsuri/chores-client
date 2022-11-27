package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.TextValue
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

expect fun getDay(
    toFormat: LocalDateTime,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): TextValue