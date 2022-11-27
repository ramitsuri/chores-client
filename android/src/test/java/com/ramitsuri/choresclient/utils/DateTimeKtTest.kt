package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.TextValue
import com.ramitsuri.choresclient.resources.LocalizedString
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeKtTest {

    private val timeZone = TimeZone.of("America/New_York")

    @Test
    fun testSnoozeDay1() {
        val now = Instant.fromEpochMilliseconds(1651406400000) // 2022-05-01 8AM UTC -4
        val actual =
            getNewReminderTimeSnoozeDay(now, timeZone).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeDay2() {
        val now = Instant.fromEpochMilliseconds(1651377600000) // 2022-05-01 00:00:00 AM UTC -4
        val actual =
            getNewReminderTimeSnoozeDay(now, timeZone).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeDay3() {
        val now = Instant.fromEpochMilliseconds(1651463999000) // 2022-05-01 23:59:59 UTC -4
        val actual =
            getNewReminderTimeSnoozeDay(now, timeZone).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour1() {
        val now = Instant.fromEpochMilliseconds(1651406400000) // 2022-05-01 8AM UTC -4
        val actual = getNewReminderTimeSnoozeHour(now, timeZone).toInstant(timeZone)
        val expected = Instant.parse("2022-05-01T18:00:00Z") // 2022-05-02 2PM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour2() {
        val now = Instant.fromEpochMilliseconds(1651377600000) // 2022-05-01 00:00:00 AM UTC -4
        val actual = getNewReminderTimeSnoozeHour(now, timeZone).toInstant(timeZone)
        val expected = Instant.parse("2022-05-01T10:00:00Z") // 2022-05-02 6AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour3() {
        val now = Instant.fromEpochMilliseconds(1651463999000) // 2022-05-01 23:59:59 UTC -4
        val actual = getNewReminderTimeSnoozeHour(now, timeZone).toInstant(timeZone)
        val expected = Instant.parse("2022-05-02T09:59:59Z") // 2022-05-02 5:59:59AM UTC -4
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
    fun testGetDay() {
        val timeZone = TimeZone.of("America/New_York")
        val now = LocalDateTime.parse("2022-11-25T11:00:00").toInstant(timeZone)

        var toFormat: LocalDateTime

        // Today
        toFormat = LocalDateTime.parse("2022-11-25T11:00:00")
        assertEquals(TextValue.ForKey(LocalizedString.TODAY), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-25T00:00:01")
        assertEquals(TextValue.ForKey(LocalizedString.TODAY), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-25T23:59:59")
        assertEquals(TextValue.ForKey(LocalizedString.TODAY), getDay(toFormat, now, timeZone))

        // Yesterday
        toFormat = LocalDateTime.parse("2022-11-24T11:00:00")
        assertEquals(TextValue.ForKey(LocalizedString.YESTERDAY), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-24T11:00:00")
        assertEquals(TextValue.ForKey(LocalizedString.YESTERDAY), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-24T11:00:00")
        assertEquals(TextValue.ForKey(LocalizedString.YESTERDAY), getDay(toFormat, now, timeZone))

        // Tomorrow
        toFormat = LocalDateTime.parse("2022-11-26T11:00:00")
        assertEquals(TextValue.ForKey(LocalizedString.TOMORROW), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-26T11:00:00")
        assertEquals(TextValue.ForKey(LocalizedString.TOMORROW), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-26T11:00:00")
        assertEquals(TextValue.ForKey(LocalizedString.TOMORROW), getDay(toFormat, now, timeZone))

        // Other days
        toFormat = LocalDateTime.parse("2022-11-23T11:00:00")
        assertEquals(TextValue.ForString("Nov 23"), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-23T11:00:00")
        assertEquals(TextValue.ForString("Nov 23"), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-23T11:00:00")
        assertEquals(TextValue.ForString("Nov 23"), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-27T11:00:00")
        assertEquals(TextValue.ForString("Nov 27"), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-27T11:00:00")
        assertEquals(TextValue.ForString("Nov 27"), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2022-11-27T11:00:00")
        assertEquals(TextValue.ForString("Nov 27"), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2023-11-27T11:00:00")
        assertEquals(TextValue.ForString("Nov 27, 2023"), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2023-11-27T11:00:00")
        assertEquals(TextValue.ForString("Nov 27, 2023"), getDay(toFormat, now, timeZone))

        toFormat = LocalDateTime.parse("2023-11-27T11:00:00")
        assertEquals(TextValue.ForString("Nov 27, 2023"), getDay(toFormat, now, timeZone))
    }
}