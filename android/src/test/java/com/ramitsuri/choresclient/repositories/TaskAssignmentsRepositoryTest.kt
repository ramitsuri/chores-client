package com.ramitsuri.choresclient.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ramitsuri.choresclient.android.testutils.FakeAlarmHandler
import com.ramitsuri.choresclient.android.testutils.FakeKeyValueStore
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.data.settings.PrefManager
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.CreateType
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.network.api.TaskAssignmentsApi
import com.ramitsuri.choresclient.reminder.ReminderScheduler
import com.ramitsuri.choresclient.utils.ContentDownloadRequestHandler
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.SyncStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime

class TaskAssignmentsRepositoryTest {

    private lateinit var database: Database
    private lateinit var prefManager: PrefManager
    private lateinit var alarmHandler: FakeAlarmHandler
    private lateinit var reminderScheduler: ReminderScheduler
    private lateinit var contentDownloadRequestHandler: FakeContentDownloadRequestHandler

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        startKoin {
            modules(module {
                single {
                    LogHelper(
                        enableLocal = false,
                        enableRemote = false,
                        deviceDetails = "test"
                    )
                }
            })
        }

        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ChoresDatabase.Schema.create(driver)
        val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, testDispatcher)
        database = Database(driver, dispatcherProvider)

        val keyValueStore = FakeKeyValueStore()
        prefManager = PrefManager(keyValueStore, keyValueStore)
        prefManager.setLoggedInMemberId("member1")

        alarmHandler = FakeAlarmHandler()
        reminderScheduler = ReminderScheduler(alarmHandler, prefManager)
        contentDownloadRequestHandler = FakeContentDownloadRequestHandler()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testMarkTaskAssignmentDone() = runTest {
        val repository = createRepository(MockEngine { respond("", HttpStatusCode.OK) })
        setupTaskAndMember()
        val entity =
            createAssignmentEntity("assignment1", ProgressStatus.TODO, shouldUpload = false)
        database.taskAssignmentDao.clearTodoAndInsert(listOf(entity))

        val doneTime = Instant.fromEpochMilliseconds(2000L)
        repository.markTaskAssignmentDone("assignment1", doneTime)

        val updated = database.taskAssignmentDao.get("assignment1")
        assertNotNull(updated)
        assertEquals(ProgressStatus.DONE, updated?.progressStatus)
        assertEquals(doneTime, updated?.progressStatusDate)
        assertTrue(updated?.shouldUpload == true)
        assertTrue(contentDownloadRequestHandler.delayedDownloadRequested)
    }

    @Test
    fun testMarkTaskAssignmentWontDo() = runTest {
        val repository = createRepository(MockEngine { respond("", HttpStatusCode.OK) })
        setupTaskAndMember()
        val entity =
            createAssignmentEntity("assignment1", ProgressStatus.TODO, shouldUpload = false)
        database.taskAssignmentDao.clearTodoAndInsert(listOf(entity))

        val wontDoTime = Instant.fromEpochMilliseconds(3000L)
        repository.markTaskAssignmentWontDo("assignment1", wontDoTime)

        val updated = database.taskAssignmentDao.get("assignment1")
        assertNotNull(updated)
        assertEquals(ProgressStatus.WONT_DO, updated?.progressStatus)
        assertEquals(wontDoTime, updated?.progressStatusDate)
        assertTrue(updated?.shouldUpload == true)
        assertTrue(contentDownloadRequestHandler.delayedDownloadRequested)
    }

    @Test
    fun testGetLocal() = runTest {
        val repository = createRepository(MockEngine { respond("", HttpStatusCode.OK) })
        setupTaskAndMember()
        val entity =
            createAssignmentEntity("assignment1", ProgressStatus.TODO, shouldUpload = false)
        database.taskAssignmentDao.clearTodoAndInsert(listOf(entity))

        val assignment = repository.getLocal("assignment1")
        assertNotNull(assignment)
        assertEquals("assignment1", assignment?.id)
        assertEquals("Clean Kitchen", assignment?.taskName)
        assertEquals("Ramit", assignment?.memberName)
    }

    @Test
    fun testGetLocalFlow() = runTest {
        val repository = createRepository(MockEngine { respond("", HttpStatusCode.OK) })
        setupTaskAndMember()
        val entity =
            createAssignmentEntity("assignment1", ProgressStatus.TODO, shouldUpload = false)
        database.taskAssignmentDao.clearTodoAndInsert(listOf(entity))

        val detailsList = repository.getLocalFlow("member1").first()
        assertEquals(1, detailsList.size)
        val details = detailsList.first()
        assertEquals("assignment1", details.taskAssignment.id)
        assertTrue(details.assignedToLoggedInUser)
    }

    @Test
    fun testGetCompletedLocalFlow() = runTest {
        val repository = createRepository(MockEngine { respond("", HttpStatusCode.OK) })
        setupTaskAndMember()
        val todoEntity =
            createAssignmentEntity("assignment1", ProgressStatus.TODO, shouldUpload = false)
        val doneEntity =
            createAssignmentEntity("assignment2", ProgressStatus.DONE, shouldUpload = false)
        val wontDoEntity =
            createAssignmentEntity("assignment3", ProgressStatus.WONT_DO, shouldUpload = false)
        database.taskAssignmentDao.insert(listOf(todoEntity, doneEntity, wontDoEntity))

        val completedDetails = repository.getCompletedLocalFlow("member1").first()
        assertEquals(2, completedDetails.size)
        val ids = completedDetails.map { it.taskAssignment.id }.toSet()
        assertTrue(ids.contains("assignment2"))
        assertTrue(ids.contains("assignment3"))
        assertEquals(false, completedDetails.first().enableSnooze)
        assertEquals(null, completedDetails.first().reminderTime)
    }

    @Test
    fun testFetchAndSaveCompleted() = runTest {
        val responseJson = """
            [
                {
                    "id": "completed1",
                    "progressStatus": "3",
                    "progressStatusDate": "2023-10-01T10:00:00Z",
                    "task": {
                        "id": "task1",
                        "name": "Clean Kitchen",
                        "description": "Clean counters",
                        "dueDateTime": "2023-10-01T10:00:00",
                        "repeatValue": 1,
                        "repeatUnit": "2",
                        "repeatEndDateTime": null,
                        "houseId": "house1",
                        "memberId": "member1",
                        "rotateMember": false,
                        "createdDate": "2023-10-01T10:00:00Z",
                        "status": 1
                    },
                    "member": {
                        "id": "member1",
                        "name": "Ramit"
                    },
                    "dueDateTime": "2023-10-01T10:00:00",
                    "createdDate": "2023-10-01T10:00:00Z",
                    "createType": "1"
                }
            ]
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            assertTrue(request.url.encodedPath == "/task-assignments/filter")
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val repository = createRepository(mockEngine)
        setupTaskAndMember()

        repository.fetchAndSaveCompleted()

        val fetched = database.taskAssignmentDao.get("completed1")
        assertNotNull(fetched)
        assertEquals("completed1", fetched?.id)
        assertEquals(ProgressStatus.DONE, fetched?.progressStatus)
    }

    @Test
    fun testRefresh() = runTest {
        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath == "/task-assignments" && request.method.value == "PUT") {
                respond(
                    content = """["upload1"]""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else if (request.url.encodedPath == "/task-assignments/filter") {
                val getResponse = """
                    [
                        {
                            "id": "fetched1",
                            "progressStatus": "1",
                            "progressStatusDate": "2023-10-01T10:00:00Z",
                            "task": {
                                "id": "task1",
                                "name": "Clean Kitchen",
                                "description": "Clean counters",
                                "dueDateTime": "2023-10-01T10:00:00",
                                "repeatValue": 1,
                                "repeatUnit": "2",
                                "repeatEndDateTime": null,
                                "houseId": "house1",
                                "memberId": "member1",
                                "rotateMember": false,
                                "createdDate": "2023-10-01T10:00:00Z",
                                "status": 1
                            },
                            "member": {
                                "id": "member1",
                                "name": "Ramit"
                            },
                            "dueDateTime": "2023-10-01T10:00:00",
                            "createdDate": "2023-10-01T10:00:00Z",
                            "createType": "1"
                        }
                    ]
                """.trimIndent()
                respond(
                    content = getResponse,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond("", HttpStatusCode.NotFound)
            }
        }

        val repository = createRepository(mockEngine)
        setupTaskAndMember()

        // Entity ready for upload
        val localUpload =
            createAssignmentEntity("upload1", ProgressStatus.DONE, shouldUpload = true)
        database.taskAssignmentDao.clearTodoAndInsert(listOf(localUpload))

        repository.refresh()

        // Uploaded entity should be marked as uploaded
        val uploaded = database.taskAssignmentDao.get("upload1")
        assertEquals(false, uploaded?.shouldUpload)

        // Fetched entity should be inserted into DAO
        val fetched = database.taskAssignmentDao.get("fetched1")
        assertNotNull(fetched)
        assertEquals("fetched1", fetched?.id)
    }

    private fun createRepository(mockEngine: MockEngine): DefaultTaskAssignmentsRepository {
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
        val api = TaskAssignmentsApi(client, "http://localhost", testDispatcher)

        return DefaultTaskAssignmentsRepository(
            api = api,
            taskAssignmentDao = database.taskAssignmentDao,
            memberDao = database.memberDao,
            taskDao = database.taskDao,
            reminderScheduler = reminderScheduler,
            alarmHandler = alarmHandler,
            contentDownloadRequestHandler = contentDownloadRequestHandler,
            memberHouseAssociationDao = database.memberHouseAssociationDao,
            prefManager = prefManager,
        )
    }

    private suspend fun setupTaskAndMember() {
        database.memberDao.clearAndInsert(
            listOf(MemberEntity(id = "member1", name = "Ramit"))
        )
        database.taskDao.clearAndInsert(
            listOf(
                TaskEntity(
                    id = "task1",
                    name = "Clean Kitchen",
                    description = "Clean counters",
                    dueDateTime = LocalDateTime.parse("2023-10-01T10:00:00"),
                    repeatValue = 1L,
                    repeatUnit = RepeatUnit.DAY,
                    repeatEndDateTime = null,
                    houseId = "house1",
                    memberId = "member1",
                    rotateMember = false,
                    createdDate = Instant.fromEpochMilliseconds(500L),
                    status = ActiveStatus.ACTIVE
                )
            )
        )
    }

    private fun createAssignmentEntity(
        id: String,
        progressStatus: ProgressStatus,
        shouldUpload: Boolean,
        shouldTransfer: Boolean = false
    ): TaskAssignmentEntity {
        return TaskAssignmentEntity(
            id = id,
            progressStatus = progressStatus,
            progressStatusDate = Instant.fromEpochMilliseconds(1000L),
            taskId = "task1",
            memberId = "member1",
            dueDateTime = LocalDateTime.parse("2023-10-01T10:00:00"),
            createDate = Instant.fromEpochMilliseconds(500L),
            createType = CreateType.AUTO,
            shouldUpload = shouldUpload,
            shouldTransfer = shouldTransfer
        )
    }

    private class FakeContentDownloadRequestHandler : ContentDownloadRequestHandler {
        var delayedDownloadRequested = false

        override fun requestImmediateDownload(
            forceDownload: Boolean,
            forceRemindPastDue: Boolean,
            forceRemindFuture: Boolean
        ): Flow<Boolean> {
            return flowOf(true)
        }

        override fun requestDelayedDownload(
            forceRemindPastDue: Boolean,
            forceRemindFuture: Boolean
        ) {
            delayedDownloadRequested = true
        }

        override fun getSyncStatusFlow(): Flow<SyncStatus> {
            return flowOf(SyncStatus())
        }
    }
}
