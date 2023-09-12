package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.error.Error
import com.ramitsuri.choresclient.utils.plus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class AddTaskViewState(
    val loading: Boolean = false,
    val taskName: String = "",
    val repeatValue: Int = 1,
    val repeatUnits: List<RepeatUnitSelectionItem> = RepeatUnit.values()
        .map { repeatUnit ->
            RepeatUnitSelectionItem(
                repeatUnit = repeatUnit,
                selected = repeatUnit == RepeatUnit.NONE
            )
        },
    val repeatEndDate: LocalDate? = null,
    val houses: List<HouseSelectionItem> = listOf(),
    val members: List<MemberSelectionItem> = listOf(),
    val now: Instant = Clock.System.now(),
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    val date: LocalDate = now.toLocalDateTime(timeZone).plus(seconds = 5 * 60).date,
    val time: LocalTime = now.toLocalDateTime(timeZone).plus(seconds = 5 * 60).time,
    val rotateMember: Boolean = false,
    val error: Error? = null,
    val enableAddTask: Boolean = false,
)
