package com.ramitsuri.choresclient.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.utils.formatReminderTime
import kotlinx.datetime.Instant

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@ReadOnlyComposable
fun formatRepeatUnit(repeatValue: Int, repeatUnit: RepeatUnit): String {
    if (repeatValue == 0 && repeatUnit != RepeatUnit.ON_COMPLETE && repeatUnit != RepeatUnit.NONE) {
        return stringResource(id = R.string.assignment_repeats_does_not_repeat)
    }
    return when (repeatUnit) {
        RepeatUnit.NONE -> {
            stringResource(id = R.string.assignment_repeats_does_not_repeat)
        }
        RepeatUnit.HOUR -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_hours,
                count = repeatValue,
                repeatValue
            )
        }
        RepeatUnit.DAY -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_days, repeatValue, repeatValue
            )
        }
        RepeatUnit.WEEK -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_weeks,
                count = repeatValue,
                repeatValue
            )
        }
        RepeatUnit.MONTH -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_months,
                count = repeatValue,
                repeatValue
            )
        }
        RepeatUnit.YEAR -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_years,
                count = repeatValue,
                repeatValue
            )
        }
        RepeatUnit.ON_COMPLETE -> {
            stringResource(id = R.string.assignment_repeats_on_complete)
        }
    }
}

@Composable
@ReadOnlyComposable
fun formatReminderAt(toFormat: Instant?): String {
    return if (toFormat != null) {
        val formatted = formatReminderTime(toFormat)
        stringResource(id = R.string.assignment_details_reminder_time, formatted)
    } else {
        stringResource(id = R.string.assignment_details_reminder_time_unavailable)
    }
}
