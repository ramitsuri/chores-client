package com.ramitsuri.choresclient.android.notification

import com.ramitsuri.choresclient.android.model.*
import com.ramitsuri.choresclient.android.testutils.FakeAlarmHandler
import com.ramitsuri.choresclient.android.testutils.FakeKeyValueStore
import com.ramitsuri.choresclient.android.testutils.FakeTaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DefaultDispatchers
import com.ramitsuri.choresclient.android.utils.PrefManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ReminderSchedulerTest {
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var taskAssignmentsRepository: FakeTaskAssignmentsRepository
    private lateinit var alarmHandler: FakeAlarmHandler
    private lateinit var prefManager: PrefManager

    @Before
    fun setUp() {
        taskAssignmentsRepository = FakeTaskAssignmentsRepository()
        alarmHandler = FakeAlarmHandler()
        prefManager = PrefManager(FakeKeyValueStore(), FakeKeyValueStore())
        reminderScheduler = ReminderScheduler(
            taskAssignmentsRepository,
            alarmHandler,
            prefManager,
            DefaultDispatchers()
        )
    }

    @Test
    fun testAddReminders_shouldAddReminder_ifDueInFuture() {
        runBlocking {
            val assignmentId = "1"
            val scheduledTime = Instant.now().plusSeconds(30)
            val memberId = "1"
            prefManager.setUserId(memberId)
            taskAssignmentsRepository.setSince(
                listOf(
                    getAssignment(
                        assignmentId,
                        dueDateTime = scheduledTime
                    )
                )
            )

            reminderScheduler.addReminders()

            assertEquals(scheduledTime.toEpochMilli(), alarmHandler.get(assignmentId))
        }
    }

    @Test
    fun testAddReminders_shouldNotAddReminder_ifDueInPast() {
        runBlocking {
            val assignmentId = "1"
            val scheduledTime = Instant.now().minusSeconds(30)
            val memberId = "1"
            prefManager.setUserId(memberId)
            taskAssignmentsRepository.setSince(
                listOf(
                    getAssignment(
                        assignmentId,
                        dueDateTime = scheduledTime
                    )
                )
            )

            reminderScheduler.addReminders()

            assertNull(alarmHandler.get(assignmentId))
        }
    }

    @Test
    fun testAddReminders_shouldNotAddReminder_ifCompleted() {
        runBlocking {
            val assignmentId = "1"
            val scheduledTime = Instant.now().plusSeconds(30)
            val memberId = "1"
            prefManager.setUserId(memberId)
            taskAssignmentsRepository.setSince(
                listOf(
                    getAssignment(
                        assignmentId,
                        dueDateTime = scheduledTime,
                        progressStatus = ProgressStatus.DONE
                    )
                )
            )

            reminderScheduler.addReminders()

            assertNull(alarmHandler.get(assignmentId))
        }
    }

    @Test
    fun testAddReminders_shouldNotAddReminder_ifProgressUnknown() {
        runBlocking {
            val assignmentId = "1"
            val scheduledTime = Instant.now().plusSeconds(30)
            val memberId = "1"
            prefManager.setUserId(memberId)
            taskAssignmentsRepository.setSince(
                listOf(
                    getAssignment(
                        assignmentId,
                        dueDateTime = scheduledTime,
                        progressStatus = ProgressStatus.UNKNOWN
                    )
                )
            )

            reminderScheduler.addReminders()

            assertNull(alarmHandler.get(assignmentId))
        }
    }

    @Test
    fun testAddReminders_shouldNotAddReminder_ifMemberDifferent() {
        runBlocking {
            val assignmentId = "1"
            val scheduledTime = Instant.now().plusSeconds(30)
            val memberId = "1"
            prefManager.setUserId("2")
            taskAssignmentsRepository.setSince(
                listOf(
                    getAssignment(
                        assignmentId,
                        dueDateTime = scheduledTime,
                        progressStatus = ProgressStatus.UNKNOWN
                    )
                )
            )

            reminderScheduler.addReminders()

            assertNull(alarmHandler.get(assignmentId))
        }
    }

    private fun getAssignment(
        assignmentId: String,
        progressStatus: ProgressStatus = ProgressStatus.TODO,
        dueDateTime: Instant = Instant.now(),
        memberId: String = "1"
    ): TaskAssignment {
        val member = Member(memberId, "member", Instant.now())
        return TaskAssignment(
            assignmentId,
            progressStatus,
            progressStatusDate = Instant.now(),
            Task(
                id = "1",
                "Name",
                "Description",
                Instant.now(),
                1,
                RepeatUnit.DAY,
                "",
                "",
                false,
                Instant.now()
            ),
            member,
            dueDateTime,
            createdDate = Instant.now(),
            CreateType.AUTO
        )
    }

    private fun getAssignments(
        numberOfAssignments: Int,
        baseDueDateTime: Instant = Instant.now(),
        padMillis: Long = (15 * 60 * 1000).toLong(),
        padDueDateTime: Boolean = true
    ): List<TaskAssignment> {
        val member = Member("1", "member", Instant.now())
        val taskAssignments = mutableListOf<TaskAssignment>()
        repeat(numberOfAssignments) {
            val index = it + 1
            taskAssignments.add(
                TaskAssignment(
                    id = index.toString(),
                    ProgressStatus.TODO,
                    progressStatusDate = Instant.now(),
                    Task(
                        id = index.toString(),
                        "Name",
                        "Description",
                        Instant.now(),
                        1,
                        RepeatUnit.DAY,
                        "",
                        "",
                        false,
                        Instant.now()
                    ),
                    member,
                    dueDateTime = if (padDueDateTime) baseDueDateTime.plusMillis(index * padMillis) else baseDueDateTime,
                    createdDate = Instant.now(),
                    CreateType.AUTO
                )
            )
        }
        return taskAssignments
    }
}