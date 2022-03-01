package com.ramitsuri.choresclient.android.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun formatInstant(
    toFormat: Instant,
    now: Instant = Instant.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String {
    val toFormatZoned = getZonedDateTime(toFormat)
    val nowZoned = getZonedDateTime(now)
    val pattern = if (toFormatZoned.year == nowZoned.year) {
        "d MMM h:mm a"
    } else {
        "d MMM, uuuu h:mm a"
    }
    val formatter = DateTimeFormatter
        .ofPattern(pattern)
        .withLocale(locale)
        .withZone(zoneId)
    return formatter.format(toFormat)
}

fun getDay(
    toFormat: Instant,
    now: Instant = Instant.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String {
    val toFormatZoned = getZonedDateTime(toFormat)
    val nowZoned = getZonedDateTime(now)
    val pattern = if (toFormatZoned.year == nowZoned.year) {
        "MMM d"
    } else {
        "MMM d, uuuu"
    }
    val formatter = DateTimeFormatter
        .ofPattern("MMM d")
        .withLocale(locale)
        .withZone(zoneId)
    return formatter.format(toFormat)
}

fun getZonedDateTime(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
    return ZonedDateTime.ofInstant(instant, zoneId)
}