package com.ramitsuri.choresclient.model.view

import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.error.Error
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class EditTaskViewState(
    val loading: Boolean = true,
    val taskName: String = "",
    val repeatValue: Int = 1,
    val repeatUnits: List<RepeatUnitSelectionItem> = RepeatUnit.values()
        .map { repeatUnit ->
            RepeatUnitSelectionItem(
                repeatUnit,
                selected = repeatUnit == RepeatUnit.NONE
            )
        },
    val repeatEndDate: LocalDate? = null,
    val now: Instant = Clock.System.now(),
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    val date: LocalDate = now.toLocalDateTime(timeZone).date,
    val time: LocalTime = now.toLocalDateTime(timeZone).time,
    val rotateMember: Boolean = false,
    val status: ActiveStatus = ActiveStatus.ACTIVE,
    val error: Error? = null,
    val enableEditTask: Boolean = false,
)
