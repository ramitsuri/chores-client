package com.ramitsuri.choresclient.android.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ramitsuri.choresclient.android.R
import com.ramitsuri.choresclient.model.TextValue
import com.ramitsuri.choresclient.resources.LocalizedString

@Composable
fun TextValue.stringValue(): String {
    return when (this) {
        is TextValue.ForString -> value
        is TextValue.ForKey -> stringResource(id = getResId(key))
    }
}

private fun getResId(key: LocalizedString): Int {
    return when (key) {
        LocalizedString.FILTER_ALL -> R.string.assignment_filter_all
        LocalizedString.ON_COMPLETION -> R.string.assignment_header_on_completion
        LocalizedString.PERSON_FILTER -> R.string.assignment_filter_person
    }
}