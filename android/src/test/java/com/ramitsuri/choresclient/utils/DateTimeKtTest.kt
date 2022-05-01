package com.ramitsuri.choresclient.utils

import kotlinx.datetime.Instant
import org.junit.Assert.*
import org.junit.Test

class DateTimeKtTest{
    @Test
    fun testSnoozeDay1() {
        val now = Instant.fromEpochMilliseconds(1651406400000) // 2022-05-01 8AM UTC -4
        val actual = getNewReminderTimeSnoozeDay(now)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeDay2() {
        val now = Instant.fromEpochMilliseconds(1651377600000) // 2022-05-01 00:00:00 AM UTC -4
        val actual = getNewReminderTimeSnoozeDay(now)
        val expected = Instant.parse("2022-05-02T12:00:00Z") // 2022-05-02 8AM UTC -4
        assertEquals(expected, actual)
    }

    @Test
    fun testSnoozeDay3() {
        val now = Instant.fromEpochMilliseconds(1651463999000) // 2022-05-01 23:59:59 UTC -4
        val actual = getNewReminderTimeSnoozeDay(now)
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
}