package com.ramitsuri.choresclient.model.enums

import kotlin.time.Duration

sealed class SnoozeType {
    data object NextBatch : SnoozeType()

    data class Custom(val inDuration: Duration) : SnoozeType()
}
