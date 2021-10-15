package com.ramitsuri.choresclient.android.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
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

fun getZonedDateTime(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
    return ZonedDateTime.ofInstant(instant, zoneId)
}

/**
 * Returns the Instant value for start of the period that the given Instant falls in when periods
 * are divided into periodLengthInMinutes intervals. Minutes should be less than 60
 *
 * For ex -
 * For instant = 1609520640000 (Fri Jan 01 2021 17:04:00 UTC) and periodLength = 15 minutes
 *  returns 1609520400000 (Fri Jan 01 2021 17:00:00 UTC)
 * For instant = 1609520640000 (Fri Jan 01 2021 17:04:00 UTC) and periodLength = 2 minutes
 *  returns 1609520640000 (Fri Jan 01 2021 17:04:00 UTC)
 * For instant = 1609523999999 (Fri Jan 01 2021 17:59:59 UTC) and periodLength = 15 minutes
 *  returns 1609523100000 (Fri Jan 01 2021 17:45:00 UTC)
 * For instant = 1609524000000 (Fri Jan 01 2021 18:00:00 UTC) and periodLength = 15 minutes
 *  returns 1609524000000 (Fri Jan 01 2021 18:00:00 UTC)
 */
fun getStartPeriodTime(instant: Instant, periodLengthInMinutes: Int = 15): Instant {
    if (periodLengthInMinutes == 0) {
        return instant
    }
    val periodLength = periodLengthInMinutes % 60
    val periodLengthMillis = periodLength * 60 * 1000

    val startOfHour = instant.atZone(ZoneOffset.UTC)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    val millisOverStartOfHour = instant.toEpochMilli() - startOfHour.toInstant().toEpochMilli()

    val periodIndex = (millisOverStartOfHour / periodLengthMillis).toInt()

    return startOfHour.withMinute(periodIndex * periodLength).toInstant()
}