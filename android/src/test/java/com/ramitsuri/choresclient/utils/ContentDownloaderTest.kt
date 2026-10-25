package com.ramitsuri.choresclient.utils

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ramitsuri.choresclient.android.testutils.FakeKeyValueStore
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.Task
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.model.enums.SnoozeType
import com.ramitsuri.choresclient.model.view.TaskAssignmentDetails
import com.ramitsuri.choresclient.model.view.TaskDetails
import com.ramitsuri.choresclient.network.api.SyncApi
import com.ramitsuri.choresclient.repositories.SyncRepository
import com.ramitsuri.choresclient.repositories.TaskAssignmentsRepository
import com.ramitsuri.choresclient.repositories.TasksRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class ContentDownloaderTest {

    private lateinit var prefManager: PrefManager
    private lateinit var syncRepository: SyncRepository
    private lateinit var fakeTaskAssignmentsRepository: FakeTaskAssignmentsRepo
    private lateinit var fakeTasksRepository: FakeTasksRepo
    private var syncApiCalled = false

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        startKoin {
            modules(module {
                single { LogHelper(enableLocal = false, enableRemote = false, deviceDetails = "test") }
            })
        }

        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ChoresDatabase.Schema.create(driver)
        val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, testDispatcher)
        val database = Database(driver, dispatcherProvider)

        val mockEngine = MockEngine { _ ->
            syncApiCalled = true
            respond(
                content = """{"associatedLists":[],"memberListAssociations":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val syncApi = SyncApi(client, "http://localhost", testDispatcher)
        syncRepository = SyncRepository(
            database.houseDao,
            database.memberHouseAssociationDao,
            database.memberDao,
            syncApi
        )

        val keyValueStore = FakeKeyValueStore()
        prefManager = PrefManager(keyValueStore, keyValueStore)
        fakeTaskAssignmentsRepository = FakeTaskAssignmentsRepo()
        fakeTasksRepository = FakeTasksRepo()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testDownloadWhenSyncThresholdNotReached() = runTest {
        val now = Clock.System.now()
        prefManager.setLastSyncTime(now.minus(12.hours))

        val downloader = ContentDownloader(
            taskAssignmentsRepository = fakeTaskAssignmentsRepository,
            tasksRepository = fakeTasksRepository,
            syncRepository = syncRepository,
            prefManager = prefManager,
            isDebug = false
        )

        downloader.download(now)

        assertFalse(syncApiCalled)
        assertFalse(fakeTasksRepository.fetchTasksCalled)
        assertTrue(fakeTaskAssignmentsRepository.refreshCalled)
    }

    @Test
    fun testDownloadWhenSyncThresholdReached() = runTest {
        val now = Clock.System.now()
        prefManager.setLastSyncTime(now.minus(7.days).minus(1.hours))

        val downloader = ContentDownloader(
            taskAssignmentsRepository = fakeTaskAssignmentsRepository,
            tasksRepository = fakeTasksRepository,
            syncRepository = syncRepository,
            prefManager = prefManager,
            isDebug = false
        )

        downloader.download(now)

        assertTrue(syncApiCalled)
        assertTrue(fakeTasksRepository.fetchTasksCalled)
        assertTrue(fakeTaskAssignmentsRepository.refreshCalled)
    }

    @Test
    fun testDownloadWhenForceDownloadTrue() = runTest {
        val now = Clock.System.now()
        prefManager.setLastSyncTime(now.minus(1.hours))

        val downloader = ContentDownloader(
            taskAssignmentsRepository = fakeTaskAssignmentsRepository,
            tasksRepository = fakeTasksRepository,
            syncRepository = syncRepository,
            prefManager = prefManager,
            isDebug = false
        )

        downloader.download(now, forceDownload = true)

        assertTrue(syncApiCalled)
        assertTrue(fakeTasksRepository.fetchTasksCalled)
        assertTrue(fakeTaskAssignmentsRepository.refreshCalled)
    }

    @Test
    fun testDownloadHandlesExceptionGracefully() = runTest {
        val now = Clock.System.now()
        fakeTaskAssignmentsRepository.shouldThrowException = true

        val downloader = ContentDownloader(
            taskAssignmentsRepository = fakeTaskAssignmentsRepository,
            tasksRepository = fakeTasksRepository,
            syncRepository = syncRepository,
            prefManager = prefManager,
            isDebug = false
        )

        // Should not throw exception
        downloader.download(now)
        assertTrue(fakeTaskAssignmentsRepository.refreshCalled)
    }

    @Test
    fun testDownloadWhenCompletedFetchThresholdReached() = runTest {
        val now = Clock.System.now()
        prefManager.setLastCompletedFetchTime(now.minus(25.hours))

        val downloader = ContentDownloader(
            taskAssignmentsRepository = fakeTaskAssignmentsRepository,
            tasksRepository = fakeTasksRepository,
            syncRepository = syncRepository,
            prefManager = prefManager,
            isDebug = false
        )

        downloader.download(now)

        assertTrue(fakeTaskAssignmentsRepository.fetchAndSaveCompletedCalled)
        assertTrue(fakeTaskAssignmentsRepository.deleteOldCompletedCalled)
    }

    @Test
    fun testDownloadWhenCompletedFetchThresholdNotReached() = runTest {
        val now = Clock.System.now()
        prefManager.setLastCompletedFetchTime(now.minus(12.hours))

        val downloader = ContentDownloader(
            taskAssignmentsRepository = fakeTaskAssignmentsRepository,
            tasksRepository = fakeTasksRepository,
            syncRepository = syncRepository,
            prefManager = prefManager,
            isDebug = false
        )

        downloader.download(now)

        assertFalse(fakeTaskAssignmentsRepository.fetchAndSaveCompletedCalled)
        assertFalse(fakeTaskAssignmentsRepository.deleteOldCompletedCalled)
    }

    private class FakeTaskAssignmentsRepo : TaskAssignmentsRepository {
        var refreshCalled = false
        var fetchAndSaveCompletedCalled = false
        var deleteOldCompletedCalled = false
        var shouldThrowException = false

        override suspend fun refresh(forceRemindPastDue: Boolean, forceRemindFuture: Boolean) {
            refreshCalled = true
            if (shouldThrowException) {
                throw RuntimeException("Network error")
            }
        }

        override fun getLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> {
            return flowOf(emptyList())
        }

        override suspend fun getLocal(id: String): TaskAssignment? = null

        override suspend fun markTaskAssignmentDone(taskAssignmentId: String, doneTime: Instant) {}

        override suspend fun markTaskAssignmentWontDo(taskAssignmentId: String, wontDoTime: Instant) {}

        override suspend fun transfer(taskAssignmentId: String) {}

        override suspend fun onSnoozeRequested(assignmentId: String, snoozeType: SnoozeType) {}

        override suspend fun fetchAndSaveCompleted() {
            fetchAndSaveCompletedCalled = true
        }

        override suspend fun deleteOldCompleted(now: Instant) {
            deleteOldCompletedCalled = true
        }

        override fun getCompletedLocalFlow(loggedInMemberId: String): Flow<List<TaskAssignmentDetails>> {
            return flowOf()
        }
    }

    private class FakeTasksRepo : TasksRepository {
        var fetchTasksCalled = false
        var uploadLocalCalled = false

        override suspend fun uploadLocal(): List<String> {
            uploadLocalCalled = true
            return emptyList()
        }

        override suspend fun fetchTasks(): Result<Unit> {
            fetchTasksCalled = true
            return Result.Success(Unit)
        }

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
}
