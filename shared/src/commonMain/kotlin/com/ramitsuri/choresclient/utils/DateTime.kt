package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.enums.SnoozeType
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

fun getNewReminderTimeSnoozeType(
    snoozeType: SnoozeType,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    morningTime: LocalTime = Defaults.MORNING_TIME,
    eveningTime: LocalTime = Defaults.EVENING_TIME,
): LocalDateTime {
    return when (snoozeType) {
        is SnoozeType.Custom -> {
            now.plus(snoozeType.inDuration).toLocalDateTime(timeZone)
        }

        is SnoozeType.NextBatch -> {
            val nowLocalDateTime = now.toLocalDateTime(timeZone)
            val todayEvening = LocalDateTime(date = nowLocalDateTime.date, time = eveningTime)
            if (nowLocalDateTime < todayEvening) {
                todayEvening
            } else {
                val tomorrowDate = now.plus(1.days).toLocalDateTime(timeZone).date
                LocalDateTime(date = tomorrowDate, time = morningTime)
            }
        }
    }
}
