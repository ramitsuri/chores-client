package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.resources.LocalizedString

sealed class TextValue {
    data class ForString(val value: String) : TextValue()
    data class ForKey(val key: LocalizedString): TextValue()
}