package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.android.testutils.FakeKeyValueStore
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.model.view.TaskDetails
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TasksRepository
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class AssignmentsViewModelTest {

    private lateinit var repository: FakeTaskAssignmentsRepository
    private lateinit var fakeTasksRepository: FakeTasksRepository
    private lateinit var fakeContentDownloadRequestHandler: FakeContentDownloadRequestHandler
    private lateinit var prefManager: PrefManager
    private lateinit var fakeClock: FakeClock
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val keyValueStore = FakeKeyValueStore()
        prefManager = PrefManager(keyValueStore, keyValueStore)
        prefManager.setLoggedInMemberId("member1")
        repository = FakeTaskAssignmentsRepository()
        fakeTasksRepository = FakeTasksRepository()
        fakeContentDownloadRequestHandler = FakeContentDownloadRequestHandler()
        fakeClock = FakeClock(1_000_000L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testMarkAsDone_addsToPendingRemovalsImmediately_andExecutesAfterDelay() = runTest(testDispatcher) {
        val viewModel = AssignmentsViewModel(
            assignmentId = null,
            repository = repository,
            tasksRepository = fakeTasksRepository,
            contentDownloadRequestHandler = fakeContentDownloadRequestHandler,
            prefManager = prefManager,
            longLivingCoroutineScope = this,
            clock = fakeClock,
        )

        backgroundScope.launch { viewModel.state.collect() }
        testScheduler.runCurrent()

        val taskId = "task-1"
        viewModel.markAsDone(taskId)
        testScheduler.runCurrent()

        val stateAfterSwipe = viewModel.state.value
        assertTrue(stateAfterSwipe.pendingRemovals.containsKey(taskId))
        assertEquals(1_000_000L, stateAfterSwipe.pendingRemovals[taskId])
        assertTrue(repository.markedDoneIds.isEmpty())

        testScheduler.advanceTimeBy(2000L)
        assertTrue(repository.markedDoneIds.isEmpty())

        testScheduler.advanceTimeBy(1100L)

        assertEquals(1, repository.markedDoneIds.size)
        assertEquals(taskId, repository.markedDoneIds[0].first)
        assertFalse(viewModel.state.value.pendingRemovals.containsKey(taskId))
    }

    @Test
    fun testMarkAsWontDo_addsToPendingRemovalsImmediately_andExecutesAfterDelay() = runTest(testDispatcher) {
        val viewModel = AssignmentsViewModel(
            assignmentId = null,
            repository = repository,
            tasksRepository = fakeTasksRepository,
            contentDownloadRequestHandler = fakeContentDownloadRequestHandler,
            prefManager = prefManager,
            longLivingCoroutineScope = this,
            clock = fakeClock,
        )

        backgroundScope.launch { viewModel.state.collect() }
        testScheduler.runCurrent()

        val taskId = "task-2"
        viewModel.markAsWontDo(taskId)
        testScheduler.runCurrent()

        val stateAfterSwipe = viewModel.state.value
        assertTrue(stateAfterSwipe.pendingRemovals.containsKey(taskId))
        assertTrue(repository.markedWontDoIds.isEmpty())

        testScheduler.advanceTimeBy(2000L)
        assertTrue(repository.markedWontDoIds.isEmpty())

        testScheduler.advanceTimeBy(1100L)

        assertEquals(1, repository.markedWontDoIds.size)
        assertEquals(taskId, repository.markedWontDoIds[0].first)
        assertFalse(viewModel.state.value.pendingRemovals.containsKey(taskId))
    }

    @Test
    fun testUndoPendingAction_cancelsJobAndRemovesFromPendingRemovals() = runTest(testDispatcher) {
        val viewModel = AssignmentsViewModel(
            assignmentId = null,
            repository = repository,
            tasksRepository = fakeTasksRepository,
            contentDownloadRequestHandler = fakeContentDownloadRequestHandler,
            prefManager = prefManager,
            longLivingCoroutineScope = this,
            clock = fakeClock,
        )

        backgroundScope.launch { viewModel.state.collect() }
        testScheduler.runCurrent()

        val taskId = "task-3"
        viewModel.markAsDone(taskId)
        testScheduler.runCurrent()

        assertTrue(viewModel.state.value.pendingRemovals.containsKey(taskId))

        viewModel.undoPendingAction(taskId)
        testScheduler.runCurrent()

        assertFalse(viewModel.state.value.pendingRemovals.containsKey(taskId))

        testScheduler.advanceTimeBy(6000L)
        assertTrue(repository.markedDoneIds.isEmpty())
    }

    private class FakeTasksRepository : TasksRepository {
        override suspend fun fetchTasks(): Result<Unit> = Result.Success(Unit)
        override suspend fun uploadLocal(): List<String> = emptyList()
        override fun getTasksFlow(): Flow<List<TaskDetails>> = flowOf(emptyList())
        override fun getUnsyncedTasksFlow(): Flow<List<TaskDetails>> = flowOf(emptyList())
        override suspend fun getTask(id: String): Task? = null
        override suspend fun addTask(
            name: String,
            description: String,
            dueDateTime: LocalDateTime,
            repeatValue: Long,
            repeatUnit: RepeatUnit,
            repeatEndDateTime: LocalDateTime?,
            houseId: String,
            memberId: String,
            rotateMember: Boolean
        ): Result<Unit> = Result.Success(Unit)

        override suspend fun editTask(
            taskId: String,
            name: String?,
            description: String?,
            dueDateTime: LocalDateTime?,
            repeatValue: Long?,
            repeatUnit: RepeatUnit?,
            repeatEndDateTime: LocalDateTime?,
            rotateMember: Boolean?,
            status: ActiveStatus?
        ): Result<Unit> = Result.Success(Unit)
    }

    private class FakeContentDownloadRequestHandler : ContentDownloadRequestHandler {
        override fun requestImmediateDownload(
            forceDownload: Boolean,
            forceRemindPastDue: Boolean,
            forceRemindFuture: Boolean
        ): Flow<Boolean> = flowOf(true)

        override fun requestDelayedDownload(
            forceRemindPastDue: Boolean,
            forceRemindFuture: Boolean
        ) {}

        override fun getSyncStatusFlow(): Flow<SyncStatus> = flowOf(SyncStatus())
    }

    private class FakeClock(private var timeMs: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(timeMs)
    }

    private class FakeTaskAssignmentsRepository : TaskAssignmentsRepository {
        val markedDoneIds = mutableListOf<Pair<String, Instant>>()
        val markedWontDoIds = mutableListOf<Pair<String, Instant>>()

        override suspend fun refresh(forceRemindPastDue: Boolean, forceRemindFuture: Boolean) {}
        override suspend fun fetchAndSaveCompleted() {}
        override suspend fun deleteOldCompleted(now: Instant) {}
        override fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> = flowOf(emptyList())
        override fun getCompletedLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> = flowOf(emptyList())
        override suspend fun getLocal(id: String): TaskAssignment? = null
        override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {
            markedDoneIds.add(Pair(taskAssignmentId, doneTime))
        }
        override suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant) {
            markedWontDoIds.add(Pair(taskAssignmentId, wontDoTime))
        }
        override suspend fun transfer(taskAssignmentId: String) {}
        override suspend fun onSnoozeRequested(assignmentId: String, snoozeType: SnoozeType) {}
    }
}
