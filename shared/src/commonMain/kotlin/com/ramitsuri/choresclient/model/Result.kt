package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.ViewError
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

data class AssignmentsViewState(
    val loading: Boolean = false,
    val assignments: Map<TextValue, List<TaskAssignmentWrapper>> = mapOf(),
    val filters: List<Filter>
)

data class LoginViewState(
    val loading: Boolean = false,
    val id: String = "",
    val key: String = "",
    val error: ViewError? = null,
    val isLoggedIn: Boolean = false,
    val loginDebugViewState: LoginDebugViewState? = null
)

data class LoginDebugViewState(
    val serverText: String = ""
)

data class AssignmentDetailsViewState(
    val loading: Boolean = false,
    val assignment: AssignmentDetails? = null
)

data class AssignmentDetails(
    val id: String,
    val name: String,
    val description: String,
    val repeatValue: Int,
    val repeatUnit: RepeatUnit,
    val notificationTime: Instant?
)

data class SettingsViewState(
    val loading: Boolean = false,
    val lastSyncTime: Instant,
    val timeZone: TimeZone,
    val error: ViewError? = null
)
