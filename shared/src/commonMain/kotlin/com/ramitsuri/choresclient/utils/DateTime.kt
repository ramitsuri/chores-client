package com.ramitsuri.choresclient.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

fun getNewReminderTimeSnoozeHour(now: Instant = Clock.System.now()): Instant {
    return now.plus(6.hours)
}

fun getNewReminderTimeSnoozeDay(
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    val tomorrowDateTime = now.plus(1.days).toLocalDateTime(timeZone)
    val reminderDateTime = LocalDateTime(
        year = tomorrowDateTime.year,
        month = tomorrowDateTime.month,
        dayOfMonth = tomorrowDateTime.dayOfMonth,
        hour = 8,
        minute = 0,
        second = 0
    )
    return reminderDateTime.toInstant(TimeZone.currentSystemDefault())
}