package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.resources.LocalizedString

enum class AssignmentsMenuItem(val id: Int, val text: TextValue) {
    SETTINGS(1, TextValue.ForKey(LocalizedString.SETTINGS))
}