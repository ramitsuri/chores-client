package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.view.TextValue
import com.ramitsuri.choresclient.resources.LocalizedString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
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
    val nowTruncated =
        now.toLocalDateTime(timeZone).toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS)
    val toFormatTruncated = toFormat.toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS)
    val daysBetweenNowAndToFormat = Duration.between(nowTruncated, toFormatTruncated).toDays()
    return when (daysBetweenNowAndToFormat) {
        0L -> {
            TextValue.ForKey(LocalizedString.TODAY)
        }

        1L -> {
            TextValue.ForKey(LocalizedString.TOMORROW)
        }

        -1L -> {
            TextValue.ForKey(LocalizedString.YESTERDAY)
        }

        else -> {
            TextValue.ForString(format(toFormat, now, timeZone, "MMM d", "MMM d, uuuu"))
        }
    }
}

actual fun differenceInDays(
    toCompare: LocalDateTime,
    now: LocalDateTime
): Int {
    val nowTruncated = now.toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS)
    val toCompareTruncated = toCompare.toJavaLocalDateTime().truncatedTo(ChronoUnit.DAYS)
    return Duration.between(nowTruncated, toCompareTruncated).toDays().toInt()
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

// Mon, Jan 5
// Mon, Jan 5, 2021
fun formatDate(
    toFormat: LocalDate,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val (formatSameYear, formatDifferentYear) = Pair("EEE, MMM d", "EEE, MMM d, uuuu")
    return format(
        LocalDateTime(date = toFormat, time = LocalDateTime.now(timeZone).time),
        now,
        timeZone,
        formatSameYear,
        formatDifferentYear
    )
}

// 4 PM
// 4:30 PM
fun formatTime(
    toFormat: LocalTime,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val jvmToFormat = toFormat.toJavaLocalTime()
    val (formatSameYear, formatDifferentYear) = if (jvmToFormat.minute == 0) {
        Pair("K a", "K a")
    } else {
        Pair("K:mm a", "K:mm a")
    }
    return format(
        LocalDateTime(date = LocalDateTime.now(timeZone).date, time = toFormat),
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