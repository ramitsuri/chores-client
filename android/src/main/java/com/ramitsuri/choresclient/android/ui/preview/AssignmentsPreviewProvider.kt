package com.ramitsuri.choresclient.android.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.ramitsuri.choresclient.data.ActiveStatus
import com.ramitsuri.choresclient.data.CreateType
import com.ramitsuri.choresclient.data.Member
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Task
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.model.TaskAssignmentWrapper
import com.ramitsuri.choresclient.model.TextValue
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AssignmentsPreview : PreviewParameterProvider<Map<TextValue, List<TaskAssignmentWrapper>>> {
    private val enableCompletedButton = true
    override val values: Sequence<Map<TextValue, List<TaskAssignmentWrapper>>>
        get() = sequenceOf(
            mapOf(
                TextValue.ForString("Oct 31") to listOf(
                    TaskAssignmentWrapper(
                        TaskAssignment(
                            id = "1",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ), enableCompletedButton
                    ),
                    TaskAssignmentWrapper(
                        TaskAssignment(
                            id = "2",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.NONE,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ), enableCompletedButton
                    ),
                    TaskAssignmentWrapper(
                        TaskAssignment(
                            id = "3",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ), enableCompletedButton
                    ),
                    TaskAssignmentWrapper(
                        TaskAssignment(
                            id = "4",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            Member(id = "", name = "Ramit", createdDate = Clock.System.now()),
                            dueDateTime = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ), enableCompletedButton
                    )
                ),
                TextValue.ForString("Nov 01") to listOf(
                    TaskAssignmentWrapper(
                        TaskAssignment(
                            id = "5",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            Member(
                                id = "",
                                name = "Ramit",
                                createdDate = Clock.System.now()
                            ),
                            dueDateTime = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ), enableCompletedButton
                    ),
                    TaskAssignmentWrapper(
                        TaskAssignment(
                            id = "6",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            Member(
                                id = "",
                                name = "Ramit",
                                createdDate = Clock.System.now()
                            ),
                            dueDateTime = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ), enableCompletedButton
                    ),
                    TaskAssignmentWrapper(
                        TaskAssignment(
                            id = "",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "7",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            Member(
                                id = "",
                                name = "Ramit",
                                createdDate = Clock.System.now()
                            ),
                            dueDateTime = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ), enableCompletedButton
                    ),
                    TaskAssignmentWrapper(
                        TaskAssignment(
                            id = "8",
                            progressStatus = ProgressStatus.TODO,
                            progressStatusDate = Clock.System.now(),
                            Task(
                                id = "",
                                name = "Clean Kitchen",
                                description = "Clean Kitchen now",
                                dueDateTime = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                                repeatValue = 2,
                                repeatUnit = RepeatUnit.DAY,
                                houseId = "",
                                memberId = "",
                                rotateMember = false,
                                createdDate = Clock.System.now(),
                                status = ActiveStatus.ACTIVE
                            ),
                            Member(
                                id = "",
                                name = "Ramit",
                                createdDate = Clock.System.now()
                            ),
                            dueDateTime = Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()),
                            createdDate = Clock.System.now(),
                            createType = CreateType.AUTO
                        ), enableCompletedButton
                    )
                )
            )
        )
}
