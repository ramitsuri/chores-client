package com.ramitsuri.choresclient.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaZoneId
import java.time.Instant as JvmInstant

actual fun getDay(
    toFormat: Instant,
    now: Instant,
    timeZone: TimeZone
): String {
    return format(toFormat, now, timeZone, "MMM d", "MMM d, uuuu")
}

fun formatReminderTime(
    toFormat: Instant,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    return format(toFormat, now, timeZone, "MMM d 'at' HH.mm.ss", "MMM d, uuuu 'at' HH.mm.ss")
}

private fun format(
    toFormat: Instant,
    now: Instant,
    timeZone: TimeZone,
    formatSameYear: String,
    formatDifferentYear: String
): String {
    val jvmToFormat = toFormat.toJavaInstant()
    val toFormatZoned = getZonedDateTime(jvmToFormat)
    val nowZoned = getZonedDateTime(now.toJavaInstant())
    val pattern = if (toFormatZoned.year == nowZoned.year) {
        formatSameYear
    } else {
        formatDifferentYear
    }
    val formatter = DateTimeFormatter
        .ofPattern(pattern)
        .withLocale(Locale.getDefault())
        .withZone(timeZone.toJavaZoneId())
    return formatter.format(jvmToFormat)
}

private fun getZonedDateTime(
    instant: JvmInstant,
    zoneId: ZoneId = ZoneId.systemDefault()
): ZonedDateTime {
    return ZonedDateTime.ofInstant(instant, zoneId)
}