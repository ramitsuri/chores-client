package com.ramitsuri.choresclient.android.notification

import com.ramitsuri.choresclient.android.testutils.FakeAlarmHandler
import com.ramitsuri.choresclient.android.testutils.FakeKeyValueStore
import com.ramitsuri.choresclient.android.testutils.FakeTaskAssignmentsRepository
import com.ramitsuri.choresclient.data.CreateType
import com.ramitsuri.choresclient.data.Member
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.Task
import com.ramitsuri.choresclient.data.TaskAssignment
import com.ramitsuri.choresclient.data.entities.AssignmentAlarm
import com.ramitsuri.choresclient.data.notification.ReminderScheduler
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
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
            DispatcherProvider()
        )
    }

    @Test
    fun testAddReminders_shouldDelete_ifAssignmentStatusCompleted() {
        runBlocking {
            // Arrange
            val assignmentId = "1"
            val duration = Duration.seconds(30)
            val scheduledTime = Clock.System.now().plus(duration)
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
            val duration = Duration.seconds(30)
            val scheduledTime = Clock.System.now().minus(duration)
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
            val duration = Duration.seconds(30)
            val scheduledTime = Clock.System.now().minus(duration)
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
            val scheduledTimeDuration = Duration.hours(25)
            alarmHandler.schedule(
                listOf(
                    AssignmentAlarm(
                        assignmentId,
                        scheduledTime.minus(scheduledTimeDuration),
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
            val duration = Duration.seconds(30)
            val scheduledTime = Clock.System.now().plus(duration)
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
            val duration = Duration.seconds(30)
            val scheduledTime = Clock.System.now().plus(duration)
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
            val duration = Duration.seconds(30)
            val scheduledTime = Clock.System.now().plus(duration)
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
        dueDateTime: Instant = Clock.System.now(),
        memberId: String = "1"
    ): TaskAssignment {
        val member = Member(memberId, "member", Clock.System.now())
        return TaskAssignment(
            assignmentId,
            progressStatus,
            progressStatusDate = Clock.System.now(),
            Task(
                id = "1",
                "Name",
                "Description",
                Clock.System.now(),
                1,
                RepeatUnit.DAY,
                "",
                "",
                false,
                Clock.System.now()
            ),
            member,
            dueDateTime,
            createdDate = Clock.System.now(),
            CreateType.AUTO
        )
    }
}