package com.ramitsuri.choresclient.utils

import com.ramitsuri.choresclient.model.view.TextValue
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

actual fun getDay(
    toFormat: LocalDateTime,
    now: Instant,
    timeZone: TimeZone
): TextValue {
    return TextValue.ForString("")
}