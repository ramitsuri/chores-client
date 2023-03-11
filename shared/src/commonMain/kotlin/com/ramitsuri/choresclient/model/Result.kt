package com.ramitsuri.choresclient.model

import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.ViewError
import com.ramitsuri.choresclient.utils.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

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
    val member: String,
    val description: String,
    val repeatValue: Int,
    val repeatUnit: RepeatUnit,
    val notificationTime: LocalDateTime?
)

data class AddEditTaskViewState(
    val loading: Boolean = false,
    val taskName: String = "",
    val taskDescription: String = "",
    val repeatValue: Int = 1,
    val repeatUnits: List<RepeatUnitSelectionItem> = RepeatUnit.values()
        .mapIndexed { index, repeatUnit ->
            RepeatUnitSelectionItem(
                repeatUnit,
                selected = index == 0
            )
        },
    val houses: List<HouseSelectionItem> = listOf(),
    val members: List<MemberSelectionItem> = listOf(),
    val date: LocalDate = LocalDateTime.now().date,
    val isDatePicked: Boolean = false,
    val time: LocalTime = LocalDateTime.now().time,
    val isTimePicked: Boolean = false,
    val taskAdded: Boolean = false,
    val enableAddTask: Boolean = false,
    val rotateMember: Boolean = false,
    val error: ViewError? = null
)
