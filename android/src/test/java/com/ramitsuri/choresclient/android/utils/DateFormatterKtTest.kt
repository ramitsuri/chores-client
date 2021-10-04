package com.ramitsuri.choresclient.android.utils

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.util.*

class DateFormatterKtTest {

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
}