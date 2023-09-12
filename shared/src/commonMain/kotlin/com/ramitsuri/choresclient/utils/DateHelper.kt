package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.view.TextValue
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

expect fun getDay(
    toFormat: LocalDateTime,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): TextValue

/**
 * Returns a negative value if toCompare is less than now
 */
expect fun differenceInDays(
    toCompare: LocalDateTime,
    now: LocalDateTime
): Int