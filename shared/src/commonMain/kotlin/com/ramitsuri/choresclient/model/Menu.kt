package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.resources.LocalizedString

enum class AssignmentsMenuItem(val id: Int, val text: TextValue) {
    SETTINGS(1, TextValue.ForKey(LocalizedString.SETTINGS))
}