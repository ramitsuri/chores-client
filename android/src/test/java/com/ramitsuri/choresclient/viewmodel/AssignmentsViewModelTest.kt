package com.ramitsuri.choresclient.viewmodel

import com.ramitsuri.choresclient.android.testutils.FakeKeyValueStore
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

        testScheduler.advanceTimeBy(4000L)
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

        testScheduler.advanceTimeBy(5100L)

        assertEquals(1, repository.markedWontDoIds.size)
        assertEquals(taskId, repository.markedWontDoIds[0].first)
        assertFalse(viewModel.state.value.pendingRemovals.containsKey(taskId))
    }

    @Test
    fun testUndoPendingAction_cancelsJobAndRemovesFromPendingRemovals() = runTest(testDispatcher) {
        val viewModel = AssignmentsViewModel(
            assignmentId = null,
            repository = repository,
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

        testScheduler.advanceTimeBy(2000L)

        viewModel.undoPendingAction(taskId)
        testScheduler.runCurrent()

        assertFalse(viewModel.state.value.pendingRemovals.containsKey(taskId))

        testScheduler.advanceTimeBy(5000L)

        assertTrue(repository.markedDoneIds.isEmpty())
    }

    private class FakeClock(var timeMillis: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(timeMillis)
    }

    private class FakeTaskAssignmentsRepository : TaskAssignmentsRepository {
        val markedDoneIds = mutableListOf<Pair<String, Instant>>()
        val markedWontDoIds = mutableListOf<Pair<String, Instant>>()
        val taskAssignmentsFlow = MutableStateFlow<List<TaskAssignmentDetails>>(emptyList())

        override fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> {
            return taskAssignmentsFlow
        }

        override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {
            markedDoneIds.add(taskAssignmentId to doneTime)
        }

        override suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant) {
            markedWontDoIds.add(taskAssignmentId to wontDoTime)
        }

        override suspend fun refresh(forceRemindPastDue: Boolean, forceRemindFuture: Boolean) {}
        override suspend fun getLocal(id: String): TaskAssignment? = null
        override suspend fun onSnoozeRequested(assignmentId: String, snoozeType: SnoozeType) {}
        override suspend fun fetchAndSaveCompleted() {}
        override suspend fun deleteOldCompleted(now: Instant) {}
        override fun getCompletedLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> = flowOf(emptyList())
    }
}
