package com.ramitsuri.choresclient.android.utils

import android.content.Context
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.utils.formatReminderTime
import kotlinx.datetime.Instant

fun Context.formatRepeatUnit(repeatValue: Int, repeatUnit: RepeatUnit): String {
    if (repeatValue == 0 && repeatUnit != RepeatUnit.ON_COMPLETE && repeatUnit != RepeatUnit.NONE) {
        return getString(R.string.assignment_repeats_does_not_repeat)
    }
    return when (repeatUnit) {
        RepeatUnit.NONE -> {
            getString(R.string.assignment_repeats_does_not_repeat)
        }
        RepeatUnit.HOUR -> {
            resources.getQuantityString(
                R.plurals.assignment_repeats_hours,
                repeatValue,
                repeatValue
            )
        }
        RepeatUnit.DAY -> {
            resources.getQuantityString(R.plurals.assignment_repeats_days, repeatValue, repeatValue)
        }
        RepeatUnit.WEEK -> {
            resources.getQuantityString(
                R.plurals.assignment_repeats_weeks,
                repeatValue,
                repeatValue
            )
        }
        RepeatUnit.MONTH -> {
            resources.getQuantityString(
                R.plurals.assignment_repeats_months,
                repeatValue,
                repeatValue
            )
        }
        RepeatUnit.YEAR -> {
            resources.getQuantityString(
                R.plurals.assignment_repeats_years,
                repeatValue,
                repeatValue
            )
        }
        RepeatUnit.ON_COMPLETE -> {
            getString(R.string.assignment_repeats_on_complete)
        }
    }
}

fun Context.formatReminderAt(toFormat: Instant?): String {
    return if (toFormat != null) {
        val formatted = formatReminderTime(toFormat)
        getString(R.string.assignment_details_reminder_time, formatted)
    } else {
        getString(R.string.assignment_details_reminder_time_unavailable)
    }
}