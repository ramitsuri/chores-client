package com.ramitsuri.choresclient.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

fun LocalDateTime.Companion.now(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
) = Clock.System.now().toLocalDateTime(timeZone)

fun LocalDateTime.plus(
    seconds: Int = 0,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime {
    return toInstant(timeZone).plus(seconds.seconds).toLocalDateTime(timeZone)
}

fun LocalDateTime.minus(
    days: Int = 0,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime {
    return toInstant(timeZone).minus(days.days).toLocalDateTime(timeZone)
}