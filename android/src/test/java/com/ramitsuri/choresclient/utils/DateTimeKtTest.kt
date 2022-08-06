package com.ramitsuri.choresclient.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeKtTest {

    private val timeZone = TimeZone.of("America/New_York")
    @Test
    fun testSnoozeDay1() {
        val now = Instant.fromEpochMilliseconds(1651406400000) // 2022-05-01 8AM UTC -4
        val actual = getNewReminderTimeSnoozeDay(now, timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeDay2() {
        val now = Instant.fromEpochMilliseconds(1651377600000) // 2022-05-01 00:00:00 AM UTC -4
        val actual = getNewReminderTimeSnoozeDay(now, timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeDay3() {
        val now = Instant.fromEpochMilliseconds(1651463999000) // 2022-05-01 23:59:59 UTC -4
        val actual = getNewReminderTimeSnoozeDay(now, timeZone)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour1() {
        val now = Instant.fromEpochMilliseconds(1651406400000) // 2022-05-01 8AM UTC -4
        val actual = getNewReminderTimeSnoozeHour(now)
        val expected = Instant.parse("2022-05-01T18:00:00Z") // 2022-05-02 2PM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour2() {
        val now = Instant.fromEpochMilliseconds(1651377600000) // 2022-05-01 00:00:00 AM UTC -4
        val actual = getNewReminderTimeSnoozeHour(now)
        val expected = Instant.parse("2022-05-01T10:00:00Z") // 2022-05-02 6AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeHour3() {
        val now = Instant.fromEpochMilliseconds(1651463999000) // 2022-05-01 23:59:59 UTC -4
        val actual = getNewReminderTimeSnoozeHour(now)
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
}