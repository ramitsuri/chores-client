package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.TextValue
import com.ramitsuri.choresclient.resources.LocalizedString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.time.Instant as JvmInstant
import java.time.LocalDateTime as JvmLocalDateTime

actual fun getDay(
    toFormat: LocalDateTime,
    now: Instant,
    timeZone: TimeZone
): TextValue {
    val nowLocal = now.toLocalDateTime(timeZone).toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS)
    val toFormatDays = toFormat.toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS)
    val difference = Duration.between(nowLocal, toFormatDays)
    return if (difference.toDays() == 0L) {
            TextValue.ForKey(LocalizedString.TODAY)
    } else if (difference.toDays() == 1L) {
        TextValue.ForKey(LocalizedString.TOMORROW)
    } else if (difference.toDays() == -1L) {
        TextValue.ForKey(LocalizedString.YESTERDAY)
    } else {
        TextValue.ForString(format(toFormat, now, timeZone, "MMM d", "MMM d, uuuu"))
    }
}

fun formatReminderTime(
    toFormat: LocalDateTime,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    return format(toFormat, now, timeZone, "MMM d 'at' K:mm a", "MMM d, uuuu 'at' K:mm a")
}

fun formatSyncTime(
    toFormat: Instant,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val jvmToFormat = toFormat.toJavaInstant()
    val toFormatZoned = getZonedDateTime(jvmToFormat, timeZone.toJavaZoneId())
    val (formatSameYear, formatDifferentYear) = if (toFormatZoned.minute == 0) {
        Pair("K a MMM d", "K a MMM d, uuuu")
    } else {
        Pair("K:mm a MMM d", "K:mm a MMM d, uuuu")
    }
    return format(
        toFormat.toLocalDateTime(timeZone),
        now,
        timeZone,
        formatSameYear,
        formatDifferentYear
    )
}

fun formatLogTime(
    toFormat: LocalDateTime = LocalDateTime.now(),
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.UTC
): String {
    return format(toFormat, now, timeZone, "uuuu-MM-dd HH.mm.ss.SSS", "uuuu-MM-dd HH.mm.ss.SSS")
}

fun formatLogParent(
    toFormat: LocalDateTime = LocalDateTime.now(),
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.UTC
): String {
    return format(toFormat, now, timeZone, "uuuu-MM-dd", "uuuu-MM-dd")
}

fun formatPickedDate(
    toFormat: LocalDate,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val toFormatDateTime = LocalDateTime(date = toFormat, time = LocalDateTime.now(timeZone).time)
    return format(toFormatDateTime, now, timeZone, "E, MMM dd, yyyy", "E, MMM dd, yyyy")
}

fun formatPickedTime(
    toFormat: LocalTime,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val toFormatDateTime = LocalDateTime(date = LocalDateTime.now(timeZone).date, time = toFormat)
    return format(toFormatDateTime, now, timeZone, "K:mm a", "K:mm a")
}

private fun format(
    toFormat: LocalDateTime,
    now: Instant,
    timeZone: TimeZone,
    formatSameYear: String,
    formatDifferentYear: String
): String {
    val jvmToFormat = toFormat.toJavaLocalDateTime()
    val toFormatZoned = getZonedDateTime(jvmToFormat, timeZone.toJavaZoneId())
    val nowZoned = getZonedDateTime(now.toJavaInstant(), timeZone.toJavaZoneId())
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
    localDateTime: JvmLocalDateTime,
    zoneId: ZoneId = ZoneId.systemDefault()
): ZonedDateTime {
    return localDateTime.atZone(zoneId)
}

private fun getZonedDateTime(
    instant: JvmInstant,
    zoneId: ZoneId = ZoneId.systemDefault()
): ZonedDateTime {
    return ZonedDateTime.ofInstant(instant, zoneId)
}