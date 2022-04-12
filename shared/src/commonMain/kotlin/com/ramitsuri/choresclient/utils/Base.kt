package com.ramitsuri.choresclient.utils

import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

object Base {
    const val API_BASE_URL = "https://chores-326817.ue.r.appspot.com"
    const val API_TIME_OUT = 60_000

    val SNOOZE_HOUR = 6.hours.inWholeSeconds
    val SNOOZE_DAY = 1.days.inWholeSeconds
}