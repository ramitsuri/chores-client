package com.ramitsuri.choresclient.android.notification

import com.ramitsuri.choresclient.android.data.AssignmentAlarm
import com.ramitsuri.choresclient.android.model.CreateType
import com.ramitsuri.choresclient.android.model.Member
import com.ramitsuri.choresclient.android.model.ProgressStatus
import com.ramitsuri.choresclient.android.model.RepeatUnit
import com.ramitsuri.choresclient.android.model.Task
import com.ramitsuri.choresclient.android.model.TaskAssignment
import com.ramitsuri.choresclient.android.testutils.FakeAlarmHandler
import com.ramitsuri.choresclient.android.testutils.FakeKeyValueStore
import com.ramitsuri.choresclient.android.testutils.FakeTaskAssignmentsRepository
import com.ramitsuri.choresclient.android.utils.DefaultDispatchers
import com.ramitsuri.choresclient.android.utils.PrefManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Duration
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
    fun testAddReminders_shouldDelete_ifAssignmentStatusCompleted() {
        runBlocking {
            // Arrange
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
            alarmHandler.schedule(listOf(AssignmentAlarm(assignmentId, scheduledTime, 1, "")))

            // Act
            reminderScheduler.addReminders()

            // Assert
            val added = alarmHandler.get(assignmentId)
            assertNull(added)
        }
    }

    @Test
    fun testAddReminders_shouldAddReminder_ifMissed() {
        runBlocking {
            // Arrange
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

            // Act
            reminderScheduler.addReminders()

            // Assert
            val added = alarmHandler.get(assignmentId)
            assertNotNull(added)
        }
    }

    @Test
    fun testAddReminders_shouldNotAddReminder_ifMissedAndLastNotifiedMoreThanADayAgo() {
        runBlocking {
            // Arrange
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
            alarmHandler.schedule(
                listOf(
                    AssignmentAlarm(
                        assignmentId,
                        scheduledTime.minusSeconds(Duration.ofHours(25).seconds),
                        100,
                        ""
                    )
                )
            )

            // Act
            reminderScheduler.addReminders()

            // Assert
            val added = alarmHandler.get(assignmentId)
            assertNotNull(added)
            assertEquals(100, added?.systemNotificationId)
        }
    }

    @Test
    fun testAddReminders_shouldNotAddReminder_ifCompleted() {
        runBlocking {
            // Arrange
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

            // Act
            reminderScheduler.addReminders()

            // Assert
            assertNull(alarmHandler.get(assignmentId))
        }
    }

    @Test
    fun testAddReminders_shouldNotAddReminder_ifProgressUnknown() {
        runBlocking {
            // Arrange
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

            // Act
            reminderScheduler.addReminders()

            // Assert
            assertNull(alarmHandler.get(assignmentId))
        }
    }

    @Test
    fun testAddReminders_shouldNotAddReminder_ifMemberDifferent() {
        runBlocking {
            // Arrange
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

            // Act
            reminderScheduler.addReminders()

            // Assert
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
}