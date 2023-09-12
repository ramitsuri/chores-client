package com.ramitsuri.choresclient.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.utils.formatReminderTime
import kotlinx.datetime.LocalDateTime

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
fun formatRepeatUnitCompact(repeatValue: Int, repeatUnit: RepeatUnit): String {
    if (repeatValue == 0 && repeatUnit != RepeatUnit.NONE) {
        return stringResource(id = R.string.assignment_repeats_does_not_repeat_compact)
    }
    return when (repeatUnit) {
        RepeatUnit.NONE -> {
            stringResource(id = R.string.assignment_repeats_does_not_repeat_compact)
        }

        RepeatUnit.HOUR -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_hours_compact,
                count = repeatValue,
                repeatValue
            )
        }

        RepeatUnit.DAY -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_days_compact, repeatValue, repeatValue
            )
        }

        RepeatUnit.WEEK -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_weeks_compact,
                count = repeatValue,
                repeatValue
            )
        }

        RepeatUnit.MONTH -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_months_compact,
                count = repeatValue,
                repeatValue
            )
        }

        RepeatUnit.YEAR -> {
            pluralStringResource(
                id = R.plurals.assignment_repeats_years_compact,
                count = repeatValue,
                repeatValue
            )
        }

        RepeatUnit.ON_COMPLETE -> {
            stringResource(id = R.string.assignment_repeats_on_complete_compact)
        }
    }
}

@Composable
@ReadOnlyComposable
fun formatReminderAt(toFormat: LocalDateTime?): String {
    return if (toFormat != null) {
        formatReminderTime(toFormat)
    } else {
        ""
    }
}