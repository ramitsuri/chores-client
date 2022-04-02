package com.ramitsuri.choresclient.utils

import java.time.Duration

object Base {
    const val API_BASE_URL = "https://chores-326817.ue.r.appspot.com"
    const val API_TIME_OUT = 60_000
    val SNOOZE_HOUR = Duration.ofHours(6).seconds
    val SNOOZE_DAY = Duration.ofDays(1).seconds
}