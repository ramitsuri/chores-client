package com.ramitsuri.choresclient.model.enums

import kotlin.time.Duration

sealed class SnoozeType {
    data object SixHours : SnoozeType()

    data object TomorrowMorning : SnoozeType()

    data class Custom(val inDuration: Duration) : SnoozeType()
}