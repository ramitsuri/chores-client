package com.ramitsuri.choresclient.utils

import kotlinx.datetime.LocalTime

object Defaults {
    const val MORNING_TIME_STRING: String = "08:00"
    const val EVENING_TIME_STRING: String = "17:00"
    val MORNING_TIME: LocalTime = LocalTime.parse(MORNING_TIME_STRING)
    val EVENING_TIME: LocalTime = LocalTime.parse(EVENING_TIME_STRING)
}
