package com.ramitsuri.choresclient.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

fun getNewReminderTimeSnoozeHour(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime {
    return now.plus(6.hours).toLocalDateTime(timeZone)
}

fun getNewReminderTimeSnoozeDay(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime {
    val tomorrowDateTime = now.plus(1.days).toLocalDateTime(timeZone)
    return LocalDateTime(
        year = tomorrowDateTime.year,
        month = tomorrowDateTime.month,
        dayOfMonth = tomorrowDateTime.dayOfMonth,
        hour = 8,
        minute = 0,
        second = 0
    )
}