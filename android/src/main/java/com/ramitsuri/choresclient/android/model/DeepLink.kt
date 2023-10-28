package com.ramitsuri.choresclient.android.model

enum class DeepLink(val uri: String) {
    ASSIGNMENT("chores://assignment"),
    COMPLETED_BY_OTHERS("chores://completed_by_others")
}