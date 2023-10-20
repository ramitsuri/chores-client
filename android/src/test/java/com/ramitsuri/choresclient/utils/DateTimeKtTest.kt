package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.TextValue
import com.ramitsuri.choresclient.resources.LocalizedString
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@Suppress("JoinDeclarationAndAssignment")
class DateTimeKtTest {

    private val timeZone = TimeZone.of("America/New_York")

    @Test
    fun testSnoozeDay1() {
        val now = Instant.fromEpochMilliseconds(1651406400000) // 2022-05-01 8AM UTC -4
        val actual = getNewReminderTimeSnoozeType(
            snoozeType = SnoozeType.TomorrowMorning,
            now,
            timeZone
        ).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeDay2() {
        val now = Instant.fromEpochMilliseconds(1651377600000) // 2022-05-01 00:00:00 AM UTC -4
        val actual = getNewReminderTimeSnoozeType(
            snoozeType = SnoozeType.TomorrowMorning,
            now,
            timeZone
        ).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeDay3() {
        val now = Instant.fromEpochMilliseconds(1651463999000) // 2022-05-01 23:59:59 UTC -4
        val actual = getNewReminderTimeSnoozeType(
            snoozeType = SnoozeType.TomorrowMorning,
            now,
            timeZone
        ).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour1() {
        val now = Instant.fromEpochMilliseconds(1651406400000) // 2022-05-01 8AM UTC -4
        val actual = getNewReminderTimeSnoozeType(
            snoozeType = SnoozeType.SixHours,
            now,
            timeZone
        ).toInstant(timeZone)
        val expected = Instant.parse("2022-05-01T18:00:00Z") // 2022-05-02 2PM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour2() {
        val now = Instant.fromEpochMilliseconds(1651377600000) // 2022-05-01 00:00:00 AM UTC -4
        val actual = getNewReminderTimeSnoozeType(
            snoozeType = SnoozeType.SixHours,
            now,
            timeZone
        ).toInstant(timeZone)
        val expected = Instant.parse("2022-05-01T10:00:00Z") // 2022-05-02 6AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour3() {
        val now = Instant.fromEpochMilliseconds(1651463999000) // 2022-05-01 23:59:59 UTC -4
        val actual = getNewReminderTimeSnoozeType(
            snoozeType = SnoozeType.SixHours,
            now,
            timeZone
        ).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T09:59:59Z") // 2022-05-02 5:59:59AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeCustomTime() {
        val now = Instant.fromEpochMilliseconds(1651463999000) // 2022-05-01 23:59:59 UTC -4
        val actual = getNewReminderTimeSnoozeType(
            snoozeType = SnoozeType.Custom(6.seconds),
            now,
            timeZone
        ).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T04:00:05Z") // 2022-05-02 00:00:05AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testFormatSyncTime() {
        val timeZone = TimeZone.of("America/New_York")
        val now = Instant.fromEpochMilliseconds(1660125600000L) // 2022-08-10, 6AM NYC

        var time = Instant.fromEpochMilliseconds(1660161600000L) // 2022-08-10, 4PM NYC
        assertEquals("4 PM Aug 10", formatSyncTime(time, now, timeZone))

        time = Instant.fromEpochMilliseconds(1660163400000L) // 2022-08-10, 4:30PM NYC
        assertEquals("4:30 PM Aug 10", formatSyncTime(time, now, timeZone))

        time = Instant.fromEpochMilliseconds(1628627400000L) // 2021-08-10, 4:30PM NYC
        assertEquals("4:30 PM Aug 10, 2021", formatSyncTime(time, now, timeZone))

        time = Instant.fromEpochMilliseconds(1628625600000L) // 2021-08-10, 4PM NYC
        assertEquals("4 PM Aug 10, 2021", formatSyncTime(time, now, timeZone))

        time = Instant.fromEpochMilliseconds(1660125600000L) // 2022-08-10, 6AM NYC
        assertEquals("6 AM Aug 10", formatSyncTime(time, now, timeZone))

        time = Instant.fromEpochMilliseconds(1660125660000L) // 2022-08-10, 6AM NYC
        assertEquals("6:01 AM Aug 10", formatSyncTime(time, now, timeZone))
    }

    @Test
    fun testFormatDate() {
        val timeZone = TimeZone.of("America/New_York")
        val now = Instant.fromEpochMilliseconds(1660125600000L) // 2022-08-10, 6AM NYC

        var date = LocalDate.parse("2022-08-10") // 2022-08-10 NYC
        assertEquals("Wed, Aug 10", formatDate(date, now, timeZone))

        date = LocalDate.parse("2021-08-10") // 2021-08-10 NYC
        assertEquals("Tue, Aug 10, 2021", formatDate(date, now, timeZone))
    }

    @Test
    fun testFormatTime() {
        val timeZone = TimeZone.of("America/New_York")
        val now = Instant.fromEpochMilliseconds(1660125600000L) // 2022-08-10, 6AM NYC

        var time = LocalTime.parse("16:00:00") // 4PM
        assertEquals("4 PM", formatTime(time, now, timeZone))

        time = LocalTime.parse("16:30:00") // 4:30PM
        assertEquals("4:30 PM", formatTime(time, now, timeZone))

        time = LocalTime.parse("06:00:00") // 6AM
        assertEquals("6 AM", formatTime(time, now, timeZone))

        time = LocalTime.parse("06:01:00") // 6:01AM
        assertEquals("6:01 AM", formatTime(time, now, timeZone))
    }

    @Test
    fun testGetDay() {
        val timeZone = TimeZone.of("America/New_York")
        val now = LocalDateTime.parse("2022-11-25T11:00:00").toInstant(timeZone)

        val assertLocalized: (LocalizedString, LocalDateTime) -> Unit = { key, inputDateTime ->
            assertEquals(TextValue.ForKey(key), getDay(inputDateTime, now, timeZone))
        }
        val assertString: (String, LocalDateTime) -> Unit = { string, inputDateTime ->
            assertEquals(TextValue.ForString(string), getDay(inputDateTime, now, timeZone))
        }

        var toFormat: LocalDateTime

        // Today
        toFormat = LocalDateTime.parse("2022-11-25T11:00:00")
        assertLocalized(LocalizedString.TODAY, toFormat)

        toFormat = LocalDateTime.parse("2022-11-25T00:00:01")
        assertLocalized(LocalizedString.TODAY, toFormat)

        toFormat = LocalDateTime.parse("2022-11-25T23:59:59")
        assertLocalized(LocalizedString.TODAY, toFormat)

        // Yesterday
        toFormat = LocalDateTime.parse("2022-11-24T11:00:00")
        assertLocalized(LocalizedString.YESTERDAY, toFormat)

        toFormat = LocalDateTime.parse("2022-11-24T00:00:01")
        assertLocalized(LocalizedString.YESTERDAY, toFormat)

        toFormat = LocalDateTime.parse("2022-11-24T23:59:59")
        assertLocalized(LocalizedString.YESTERDAY, toFormat)

        // Tomorrow
        toFormat = LocalDateTime.parse("2022-11-26T11:00:00")
        assertLocalized(LocalizedString.TOMORROW, toFormat)

        toFormat = LocalDateTime.parse("2022-11-26T00:00:01")
        assertLocalized(LocalizedString.TOMORROW, toFormat)

        toFormat = LocalDateTime.parse("2022-11-26T23:59:59")
        assertLocalized(LocalizedString.TOMORROW, toFormat)

        // Other days
        toFormat = LocalDateTime.parse("2022-11-23T11:00:00")
        assertString("Nov 23", toFormat)

        toFormat = LocalDateTime.parse("2022-11-23T00:00:01")
        assertString("Nov 23", toFormat)

        toFormat = LocalDateTime.parse("2022-11-23T23:59:59")
        assertString("Nov 23", toFormat)

        toFormat = LocalDateTime.parse("2022-11-27T11:00:00")
        assertString("Nov 27", toFormat)

        toFormat = LocalDateTime.parse("2022-11-27T00:00:01")
        assertString("Nov 27", toFormat)

        toFormat = LocalDateTime.parse("2022-11-27T23:59:59")
        assertString("Nov 27", toFormat)

        toFormat = LocalDateTime.parse("2023-11-27T11:00:00")
        assertString("Nov 27, 2023", toFormat)

        toFormat = LocalDateTime.parse("2023-11-27T00:00:01")
        assertString("Nov 27, 2023", toFormat)

        toFormat = LocalDateTime.parse("2023-11-27T23:59:59")
        assertString("Nov 27, 2023", toFormat)
    }

    @Test
    fun testCompareDay() {
        val now = LocalDateTime.parse("2022-11-25T11:00:00")
        var toCompare: LocalDateTime

        toCompare = LocalDateTime.parse("2022-11-25T11:00:00")
        assertEquals(0, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-25T00:00:00")
        assertEquals(0, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-25T23:59:59")
        assertEquals(0, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-24T11:00:00")
        assertEquals(-1, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-24T00:00:00")
        assertEquals(-1, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-24T23:59:59")
        assertEquals(-1, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-26T11:00:00")
        assertEquals(1, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-26T00:00:00")
        assertEquals(1, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-26T23:59:59")
        assertEquals(1, differenceInDays(toCompare, now))

        toCompare = LocalDateTime.parse("2022-11-28T23:59:59")
        assertEquals(3, differenceInDays(toCompare, now))
    }
}