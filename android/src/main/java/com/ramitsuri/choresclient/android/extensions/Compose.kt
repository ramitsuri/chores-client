package com.ramitsuri.choresclient.android.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.model.view.TextValue
import com.ramitsuri.choresclient.resources.LocalizedString

@Composable
fun TextValue.string(): String {
    return when (this) {
        is TextValue.ForString -> {
            value.plus(args.joinToString(separator = ""))
        }
        is TextValue.ForKey -> {
            stringResource(id = getResId(key)).plus(args.joinToString(separator = ""))
        }
    }
}

private fun getResId(key: LocalizedString): Int {
    return when (key) {
        LocalizedString.FILTER_ALL -> R.string.assignment_filter_all
        LocalizedString.ON_COMPLETION -> R.string.assignment_header_on_completion
        LocalizedString.PERSON_FILTER -> R.string.assignment_filter_person
        LocalizedString.HOUSE_FILTER -> R.string.assignment_filter_house
        LocalizedString.SETTINGS -> R.string.settings
        LocalizedString.REPEAT_UNIT_NONE -> R.string.repeat_unit_none
        LocalizedString.REPEAT_UNIT_DAY -> R.string.repeat_unit_day
        LocalizedString.REPEAT_UNIT_WEEK -> R.string.repeat_unit_week
        LocalizedString.REPEAT_UNIT_MONTH -> R.string.repeat_unit_month
        LocalizedString.REPEAT_UNIT_HOUR -> R.string.repeat_unit_hour
        LocalizedString.REPEAT_UNIT_YEAR -> R.string.repeat_unit_year
        LocalizedString.REPEAT_UNIT_ON_COMPLETE -> R.string.repeat_unit_on_complete
        LocalizedString.TODAY -> R.string.today
        LocalizedString.TOMORROW -> R.string.tomorrow
        LocalizedString.YESTERDAY -> R.string.yesterday
        LocalizedString.NOTIFICATION_ACTION_SNOOZE_HOUR -> R.string.settings_notification_action_snooze_hours
        LocalizedString.NOTIFICATION_ACTION_SNOOZE_DAY -> R.string.settings_notification_action_snooze_day
        LocalizedString.NOTIFICATION_ACTION_COMPLETE -> R.string.settings_notification_action_complete
        LocalizedString.NOTIFICATION_ACTION_WONT_DO -> R.string.settings_notification_action_wont_do
    }
}

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }
