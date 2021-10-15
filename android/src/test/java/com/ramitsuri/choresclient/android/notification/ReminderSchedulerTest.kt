package com.ramitsuri.choresclient.android.notification

import com.ramitsuri.choresclient.android.data.ReminderAssignment
import com.ramitsuri.choresclient.android.data.ReminderAssignmentUpdateResult
import com.ramitsuri.choresclient.android.model.CreateType
import com.ramitsuri.choresclient.android.model.Member
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.Task
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.testutils.FakeAlarmHandler
import com.ramitsuri.choresclient.android.testutils.FakeReminderAssignmentDao
import com.ramitsuri.choresclient.android.utils.DefaultDispatchers
import com.ramitsuri.choresclient.android.utils.getStartPeriodTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZonedDateTime

class ReminderSchedulerTest {
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var reminderAssignmentDao: FakeReminderAssignmentDao
    private lateinit var alarmHandler: FakeAlarmHandler

    @Before
    fun setUp() {
        reminderAssignmentDao = FakeReminderAssignmentDao()
        alarmHandler = FakeAlarmHandler()
        reminderScheduler = ReminderScheduler(
            reminderAssignmentDao,
            alarmHandler,
            DefaultDispatchers()
        ) {
            dueDateTimeModifier(it)
        }
    }

    @Test
    fun testAddNewReminders_shouldAddReminderForDueDateTime_ifAddingNew() {
        runBlocking {
            // Arrange
            val assignments = getAssignments(numberOfAssignments = 1)
            val scheduledTime = dueDateTimeModifier(assignments[0].dueDateTime).toEpochMilli()

            // Act
            reminderScheduler.addReminders(assignments)

            // Assert
            assertEquals(1, alarmHandler.getScheduledTimes().size)
            assertTrue(alarmHandler.scheduledForTime(scheduledTime))
        }
    }

    @Test
    fun testAddNewReminders_shouldAddOneReminder_ifAddingNewAndAssignmentsHaveSameScheduledDateTime() {
        runBlocking {
            // Arrange
            val baseDueDateTime = ZonedDateTime.now()
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
            val assignments = getAssignments(
                numberOfAssignments = 2,
                baseDueDateTime = baseDueDateTime.toInstant(),
                padMillis = (2 * 60 * 1000).toLong()
            )
            val scheduledTime = dueDateTimeModifier(assignments[0].dueDateTime).toEpochMilli()

            // Act
            reminderScheduler.addReminders(assignments)

            // Assert
            assertEquals(1, alarmHandler.getScheduledTimes().size)
            assertTrue(alarmHandler.scheduledForTime(scheduledTime))
        }
    }

    @Test
    fun testAddNewReminders_shouldAddMultipleReminders_ifAddingNewAndAssignmentsHaveDifferentScheduledDateTime() {
        runBlocking {
            // Arrange
            val baseDueDateTime = ZonedDateTime.now()
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
            val assignments = getAssignments(
                numberOfAssignments = 2,
                baseDueDateTime = baseDueDateTime.toInstant()
            )
            val scheduledTime1 = dueDateTimeModifier(assignments[0].dueDateTime).toEpochMilli()
            val scheduledTime2 = dueDateTimeModifier(assignments[1].dueDateTime).toEpochMilli()

            // Act
            reminderScheduler.addReminders(assignments)

            // Assert
            assertEquals(2, alarmHandler.getScheduledTimes().size)
            assertTrue(alarmHandler.scheduledForTime(scheduledTime1))
            assertTrue(alarmHandler.scheduledForTime(scheduledTime2))
        }
    }

    @Test
    fun testAddNewReminders_shouldCancelOldReminderAndAddNewReminder_ifUpdatingAssignmentWithDifferentTimeAndOldTimeHasNoReminders() {
        runBlocking {
            // Arrange
            val baseDueDateTime = ZonedDateTime.now()
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

            val initialDueDateTime = baseDueDateTime.minusHours(1).toInstant()
            val newDueDateTime = baseDueDateTime.toInstant()
            var assignments = getAssignments(
                numberOfAssignments = 1,
                baseDueDateTime = initialDueDateTime
            )
            reminderScheduler.addReminders(assignments)
            reminderAssignmentDao.setFakeResult(
                ReminderAssignmentUpdateResult(
                    ReminderAssignment(
                        assignmentId = "1",
                        time = newDueDateTime.toEpochMilli(),
                        requestCode = 1
                    ),
                    true
                )
            )

            // Act
            assignments = getAssignments(
                numberOfAssignments = 1,
                baseDueDateTime = newDueDateTime
            )
            reminderScheduler.addReminders(assignments)

            // Assert
            assertEquals(1, alarmHandler.getScheduledTimes().size)
            assertTrue(alarmHandler.scheduledForTime(newDueDateTime.toEpochMilli()))
            assertFalse(alarmHandler.scheduledForTime(initialDueDateTime.toEpochMilli()))
        }
    }

    @Test
    fun testAddNewReminders_shouldNotCancelOldReminderAndAddNewReminder_ifUpdatingAssignmentWithDifferentTimeAndOldTimeHasReminders() {
        runBlocking {
            // Arrange
            val baseDueDateTime = ZonedDateTime.now()
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

            val initialDueDateTime = baseDueDateTime.minusHours(1).toInstant()
            val newDueDateTime = baseDueDateTime.toInstant()
            var assignments = getAssignments(
                numberOfAssignments = 2,
                baseDueDateTime = initialDueDateTime,
                padDueDateTime = false // Both assignments will have the same due date time
            )
            reminderScheduler.addReminders(assignments)
            reminderAssignmentDao.setFakeResult(
                ReminderAssignmentUpdateResult(
                    ReminderAssignment(
                        assignmentId = "1",
                        time = newDueDateTime.toEpochMilli(),
                        requestCode = 1
                    ),
                    false
                )
            )

            // Act
            assignments = getAssignments(
                numberOfAssignments = 1,
                baseDueDateTime = newDueDateTime
            )
            reminderScheduler.addReminders(assignments)

            // Assert
            assertEquals(2, alarmHandler.getScheduledTimes().size)
            assertTrue(alarmHandler.scheduledForTime(newDueDateTime.toEpochMilli()))
            assertTrue(alarmHandler.scheduledForTime(initialDueDateTime.toEpochMilli()))
        }
    }

    private fun dueDateTimeModifier(
        value: Instant,
        periodLengthInMinutes: Int = 15
    ) = getStartPeriodTime(value, periodLengthInMinutes)

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