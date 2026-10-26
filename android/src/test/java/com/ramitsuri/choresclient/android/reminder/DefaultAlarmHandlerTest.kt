package com.ramitsuri.choresclient.android.reminder

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ramitsuri.choresclient.android.testutils.FakeKeyValueStore
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.data.db.dao.AlarmDao
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.model.entities.AssignmentAlarm
import com.ramitsuri.choresclient.model.entities.RepeatInfo
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.notification.NotificationChannelInfo
import com.ramitsuri.choresclient.notification.NotificationInfo
import com.ramitsuri.choresclient.notification.NotificationManager
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.minus
import com.ramitsuri.choresclient.utils.now
import com.ramitsuri.choresclient.utils.plus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Instant

class DefaultAlarmHandlerTest {

    private lateinit var database: Database
    private lateinit var alarmDao: AlarmDao
    private lateinit var fakeNotificationManager: FakeNotificationManager
    private lateinit var fakeAlarmScheduler: FakeAlarmScheduler
    private lateinit var fakeRepository: FakeTaskAssignmentsRepository
    private lateinit var prefManager: PrefManager
    private lateinit var defaultAlarmHandler: DefaultAlarmHandler

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        stopKoin()
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ChoresDatabase.Schema.create(driver)
        val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, testDispatcher)
        database = Database(driver, dispatcherProvider)
        alarmDao = database.alarmDao

        fakeNotificationManager = FakeNotificationManager()
        fakeAlarmScheduler = FakeAlarmScheduler()
        fakeRepository = FakeTaskAssignmentsRepository()
        val keyValueStore = FakeKeyValueStore()
        prefManager = PrefManager(keyValueStore, keyValueStore)

        startKoin {
            modules(module {
                single {
                    LogHelper(
                        enableLocal = false,
                        enableRemote = false,
                        deviceDetails = "test"
                    )
                }
                single<TaskAssignmentsRepository> { fakeRepository }
                single<PrefManager> { prefManager }
            })
        }

        defaultAlarmHandler = DefaultAlarmHandler(
            notificationManager = fakeNotificationManager,
            alarmDao = alarmDao,
            alarmScheduler = fakeAlarmScheduler,
            channelId = "notification_reminders_id"
        )
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testScheduleFutureAlarmsDoesNotPostNotifications() = runTest {
        val now = LocalDateTime.now()
        val futureTime1 = now.plus(seconds = 3600) // +1 hour
        val futureTime2 = now.plus(seconds = 7200) // +2 hours

        val alarms = listOf(
            AssignmentAlarm(
                assignmentId = "task1",
                showAtTime = futureTime2,
                systemNotificationId = 1
            ),
            AssignmentAlarm(
                assignmentId = "task2",
                showAtTime = futureTime1,
                systemNotificationId = 2
            )
        )

        defaultAlarmHandler.schedule(alarms)

        // 1. AlarmDao stores both alarms
        val storedAlarms = alarmDao.get()
        assertEquals(2, storedAlarms.size)

        // 2. AlarmScheduler schedules the earliest future alarm (futureTime1)
        assertEquals(futureTime1, fakeAlarmScheduler.scheduledTime)

        // 3. ZERO notifications posted on schedule / background refresh
        assertTrue(fakeNotificationManager.postedNotifications.isEmpty())
    }

    @Test
    fun testOnAlarmFiredPostsNotificationAndAdvancesShowAtTimeByOneDay() = runTest {
        val now = LocalDateTime.now()
        val dueTime = now.minus(days = 0) // Due right now
        val task = createTestTaskAssignment(id = "task1", name = "Clean Kitchen")
        fakeRepository.tasks["task1"] = task

        val alarm = AssignmentAlarm(
            assignmentId = "task1",
            showAtTime = dueTime,
            systemNotificationId = 100
        )
        defaultAlarmHandler.schedule(listOf(alarm))

        assertEquals(0, fakeNotificationManager.postedNotifications.size)

        // Simulate AlarmManager firing broadcast
        defaultAlarmHandler.onAlarmFired()

        // 1. Notification posted for due task
        assertEquals(1, fakeNotificationManager.postedNotifications.size)

        // 2. AlarmDao updated to advance showAtTime by 1 day
        val updatedAlarm = alarmDao.get("task1")
        assertNotNull(updatedAlarm)
        val expectedNextRepeatTime = dueTime.plus(days = 1)
        assertEquals(expectedNextRepeatTime, updatedAlarm?.showAtTime)

        // 3. AlarmScheduler rescheduled for the new advanced showAtTime
        assertEquals(expectedNextRepeatTime, fakeAlarmScheduler.scheduledTime)
    }

    @Test
    fun testBackgroundRefreshDoesNotReshowAlreadyFiredNotification() = runTest {
        val now = LocalDateTime.now()
        val dueTime = now.minus(days = 0)
        val task = createTestTaskAssignment(id = "task1", name = "Vacuum")
        fakeRepository.tasks["task1"] = task

        val alarm = AssignmentAlarm(
            assignmentId = "task1",
            showAtTime = dueTime,
            systemNotificationId = 101
        )
        defaultAlarmHandler.schedule(listOf(alarm))

        // Alarm fires
        defaultAlarmHandler.onAlarmFired()
        assertEquals(1, fakeNotificationManager.postedNotifications.size)

        // Simulate a background content download / refresh calling schedule()
        val futureTime2 = now.plus(seconds = 3600)
        defaultAlarmHandler.schedule(
            listOf(
                AssignmentAlarm(
                    "task2",
                    showAtTime = futureTime2,
                    systemNotificationId = 102
                )
            )
        )

        // NO new notifications posted for task1 on refresh!
        assertEquals(1, fakeNotificationManager.postedNotifications.size)
    }

    @Suppress("SameParameterValue")
    private fun createTestTaskAssignment(
        id: String,
        name: String,
    ): TaskAssignment {
        val nowInstant = Clock.System.now()
        return TaskAssignment(
            id = id,
            progressStatus = ProgressStatus.TODO,
            progressStatusDate = nowInstant,
            taskId = "t1",
            taskName = name,
            taskDescription = "",
            houseId = "h1",
            repeatInfo = RepeatInfo(
                repeatValue = 1,
                repeatUnit = RepeatUnit.DAY,
                repeatEndDateTime = null
            ),
            memberId = "m1",
            memberName = "Member",
            dueDateTime = LocalDateTime.now()
        )
    }
}

class FakeAlarmScheduler : AlarmScheduler {
    var scheduledTime: LocalDateTime? = null

    override fun scheduleAlarm(showAtTime: LocalDateTime) {
        scheduledTime = showAtTime
    }

    override fun cancelAlarm() {
        scheduledTime = null
    }
}

class FakeNotificationManager : NotificationManager {
    val postedNotifications = mutableListOf<NotificationInfo>()
    val cancelledIds = mutableListOf<Int>()

    override fun createChannels(channels: List<NotificationChannelInfo>) {}

    override fun showNotification(notificationInfo: NotificationInfo) {
        postedNotifications.add(notificationInfo)
    }

    override fun cancelNotification(notificationId: Int) {
        cancelledIds.add(notificationId)
    }
}

class FakeTaskAssignmentsRepository : TaskAssignmentsRepository {
    val tasks = mutableMapOf<String, TaskAssignment>()

    override suspend fun refresh(forceRemindPastDue: Boolean, forceRemindFuture: Boolean) {}
    override suspend fun fetchAndSaveCompleted() {}
    override suspend fun deleteOldCompleted(now: Instant) {}
    override fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> =
        flowOf(emptyList())

    override fun getCompletedLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> =
        flowOf(emptyList())

    override suspend fun getLocal(id: String): TaskAssignment? = tasks[id]
    override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {}
    override suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant) {}
    override suspend fun transfer(taskAssignmentId: String) {}
    override suspend fun onSnoozeRequested(assignmentId: String, snoozeType: SnoozeType) {}
}
