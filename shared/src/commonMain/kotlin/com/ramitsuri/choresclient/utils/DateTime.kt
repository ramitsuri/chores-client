package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.enums.SnoozeType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

fun getNewReminderTimeSnoozeType(
    snoozeType: SnoozeType,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime {
    return when (snoozeType) {
        is SnoozeType.Custom -> {
            now.plus(snoozeType.inDuration).toLocalDateTime(timeZone)
        }

        is SnoozeType.TomorrowMorning -> {
            val tomorrowDateTime = now.plus(1.days).toLocalDateTime(timeZone)
            LocalDateTime(
                date = tomorrowDateTime.date,
                time = LocalTime(hour = 8, minute = 0)
            )
        }

        is SnoozeType.SixHours -> {
            now.plus(6.hours).toLocalDateTime(timeZone)
        }
    }
}