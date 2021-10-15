package com.ramitsuri.choresclient.android.utils

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.*

class DateHelperKtTest {

    private val zoneId = ZoneId.of("America/New_York")
    private val locale = Locale.US
    private val now = Instant.ofEpochMilli(1614704400000) // Tue Mar 02 2021 17:00:00 UTC

    @Before
    fun setUp() {
    }

    @Test
    fun formatDate_shouldFormatCorrectly_ifSameYearAndNonDaylightSaving() {
        val toFormat = Instant.ofEpochMilli(1614618000000) // Mon Mar 01 2021 17:00:00 UTC
        val expected = "1 Mar 12:00 PM"
        assertEquals(expected, formatInstant(toFormat, now, zoneId, locale))
    }

    @Test
    fun formatDate_shouldFormatCorrectly_ifDifferentYearAndNonDaylightSaving() {
        val toFormat = Instant.ofEpochMilli(1583082000000) // Sun Mar 01 2020 17:00:00 UTC
        val expected = "1 Mar, 2020 12:00 PM"
        assertEquals(expected, formatInstant(toFormat, now, zoneId, locale))
    }

    @Test
    fun formatDate_shouldFormatCorrectly_ifSameYearAndDaylightSaving() {
        val toFormat = Instant.ofEpochMilli(1615824000000) // Mon Mar 15 2021 16:00:00 UTC
        val expected = "15 Mar 12:00 PM"
        assertEquals(expected, formatInstant(toFormat, now, zoneId, locale))
    }

    @Test
    fun formatDate_shouldFormatCorrectly_ifDifferentYearAndDaylightSaving() {
        val toFormat = Instant.ofEpochMilli(1584288000000) // Sun Mar 15 2020 16:00:00 UTC
        val expected = "15 Mar, 2020 12:00 PM"
        assertEquals(expected, formatInstant(toFormat, now, zoneId, locale))
    }

    @Test
    fun getStartPeriodTime_ifPeriodLengthComfortablyBiggerThanMinutesInGivenInstant() {
        val instant = Instant.parse("2021-01-01T17:04:00.000Z")
        val expected = Instant.parse("2021-01-01T17:00:00.000Z")
        assertEquals(
            expected,
            getStartPeriodTime(instant, 5)
        )
    }

    @Test
    fun getStartPeriodTime_ifPeriodLengthSameAsMinutesInGivenInstant() {
        val instant = Instant.parse("2021-01-01T17:04:00.000Z")
        val expected = Instant.parse("2021-01-01T17:04:00.000Z")
        assertEquals(
            expected,
            getStartPeriodTime(instant, 4)
        )
    }

    @Test
    fun getStartPeriodTime_ifPeriodLengthSlightlySmallerThanMinutesInGivenInstant() {
        val instant = Instant.parse("2021-01-01T17:04:00.001Z")
        val expected = Instant.parse("2021-01-01T17:04:00.000Z")
        assertEquals(
            expected,
            getStartPeriodTime(instant, 4)
        )
    }

    @Test
    fun getStartPeriodTime_ifPeriodLengthComfortablySmallerThanMinutesInGivenInstant() {
        val instant = Instant.parse("2021-01-01T17:03:59.999Z")
        val expected = Instant.parse("2021-01-01T17:00:00.000Z")
        assertEquals(
            expected,
            getStartPeriodTime(instant, 4)
        )
    }
}