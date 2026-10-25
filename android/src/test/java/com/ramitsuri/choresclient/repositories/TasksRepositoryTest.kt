package com.ramitsuri.choresclient.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.db.HouseEntity
import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.db.TaskOutboxEntity
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
import com.ramitsuri.choresclient.network.api.TasksApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import com.ramitsuri.choresclient.utils.SyncStatus
import com.ramitsuri.choresclient.utils.TaskUploadRequestHandler
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
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
import kotlin.time.Clock
import kotlin.time.Instant

class TasksRepositoryTest {

    private lateinit var database: Database
    private lateinit var fakeTaskUploadRequestHandler: FakeTaskUploadRequestHandler

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
        val dispatcherProvider = DispatcherProvider()
        database = Database(driver, dispatcherProvider)
        fakeTaskUploadRequestHandler = FakeTaskUploadRequestHandler()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun testFetchTasksSuccess() = runTest {
        val responseJson = """
            [
                {
                    "id": "task1",
                    "name": "Clean Kitchen",
                    "description": "Clean counters",
                    "dueDateTime": "2023-10-01T10:00:00",
                    "repeatValue": 1,
                    "repeatUnit": "2",
                    "repeatEndDateTime": null,
                    "houseId": "house1",
                    "memberId": "member1",
                    "rotateMember": true,
                    "createdDate": "2023-10-01T10:00:00Z",
                    "status": 1
                }
            ]
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath == "/tasks/batch") {
                respond(
                    content = """{"successfulRequestIds":[],"deleteTaskAssignmentsTaskIds":[]}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                assertEquals("/tasks", request.url.encodedPath)
                respond(
                    content = responseJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val api = TasksApi(client, "http://localhost", Dispatchers.Unconfined)

        val repository = DefaultTasksRepository(
            api = api,
            taskDao = database.taskDao,
            taskOutboxDao = database.taskOutboxDao,
            taskAssignmentDao = database.taskAssignmentDao,
            memberDao = database.memberDao,
            houseDao = database.houseDao,
            memberHouseAssociationDao = database.memberHouseAssociationDao,
            taskUploadRequestHandler = fakeTaskUploadRequestHandler,
        )

        val result = repository.fetchTasks()
        assertTrue(result is Result.Success)

        val savedTask = database.taskDao.get("task1")
        assertNotNull(savedTask)
        assertEquals("Clean Kitchen", savedTask?.name)
    }

    @Test
    fun testGetTasksFlowResolvesMemberAndHouseNames() = runTest {
        database.memberDao.clearAndInsert(listOf(MemberEntity("member1", "Ramit")))
        database.houseDao.clearAndInsert(
            listOf(
                HouseEntity(
                    id = "house1",
                    name = "Home",
                    createdByMemberId = "member1",
                    createdDate = Instant.fromEpochMilliseconds(1000L),
                    status = ActiveStatus.ACTIVE
                )
            )
        )

        val responseJson = """
            [
                {
                    "id": "task1",
                    "name": "Clean Kitchen",
                    "description": "Clean counters",
                    "dueDateTime": "2023-10-01T10:00:00",
                    "repeatValue": 1,
                    "repeatUnit": "2",
                    "repeatEndDateTime": null,
                    "houseId": "house1",
                    "memberId": "member1",
                    "rotateMember": true,
                    "createdDate": "2023-10-01T10:00:00Z",
                    "status": 1
                }
            ]
        """.trimIndent()

        val fetchApi = TasksApi(
            HttpClient(MockEngine { request ->
                respond(
                    if (request.url.encodedPath == "/tasks/batch") """{"successfulRequestIds":[],"deleteTaskAssignmentsTaskIds":[]}""" else responseJson,
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json")
                )
            }) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } },
            "http://localhost",
            Dispatchers.Unconfined
        )
        val fetchRepo = DefaultTasksRepository(
            fetchApi,
            database.taskDao,
            database.taskOutboxDao,
            database.taskAssignmentDao,
            database.memberDao,
            database.houseDao,
            memberHouseAssociationDao = database.memberHouseAssociationDao,
            taskUploadRequestHandler = fakeTaskUploadRequestHandler,
        )
        fetchRepo.fetchTasks()

        val taskDetailsList = fetchRepo.getTasksFlow().first()
        assertEquals(1, taskDetailsList.size)
        val details = taskDetailsList.first()
        assertEquals("task1", details.task.id)
        assertEquals("Ramit", details.memberName)
        assertEquals("Home", details.houseName)
    }

    @Test
    fun testAddTaskSavesToOutboxAndRequestsSync() = runTest {
        val mockEngine = MockEngine { respond("""{"successfulRequestIds":[],"deleteTaskAssignmentsTaskIds":[]}""", HttpStatusCode.OK) }
        val client = HttpClient(mockEngine)
        val api = TasksApi(client, "http://localhost", Dispatchers.Unconfined)

        val repository = DefaultTasksRepository(
            api = api,
            taskDao = database.taskDao,
            taskOutboxDao = database.taskOutboxDao,
            taskAssignmentDao = database.taskAssignmentDao,
            memberDao = database.memberDao,
            houseDao = database.houseDao,
            memberHouseAssociationDao = database.memberHouseAssociationDao,
            taskUploadRequestHandler = fakeTaskUploadRequestHandler,
        )

        val result = repository.addTask(
            name = "Test Task",
            description = "Test Description",
            dueDateTime = LocalDateTime(2025, 1, 15, 10, 0),
            repeatValue = 1,
            repeatUnit = RepeatUnit.DAY,
            repeatEndDateTime = null,
            houseId = "house1",
            memberId = "member1",
            rotateMember = false
        )

        assertTrue(result is Result.Success)
        assertTrue(fakeTaskUploadRequestHandler.delayedUploadRequested)

        val outboxItems = database.taskOutboxDao.getOutboxItems()
        assertEquals(1, outboxItems.size)
        val pendingTask = outboxItems.first()
        assertEquals("Test Task", pendingTask.name)
        assertEquals("add", pendingTask.type)
    }

    @Test
    fun testEditTaskSavesToOutboxAndRequestsSync() = runTest {
        val existingTask = TaskEntity(
            id = "task1",
            name = "Original Name",
            description = "Desc",
            dueDateTime = LocalDateTime(2025, 1, 15, 10, 0),
            repeatValue = 1,
            repeatUnit = RepeatUnit.DAY,
            repeatEndDateTime = null,
            houseId = "house1",
            memberId = "member1",
            rotateMember = false,
            createdDate = Instant.fromEpochMilliseconds(1000L),
            status = ActiveStatus.ACTIVE
        )
        database.taskDao.insert(existingTask)

        val mockEngine = MockEngine { respond("""{"successfulRequestIds":[],"deleteTaskAssignmentsTaskIds":[]}""", HttpStatusCode.OK) }
        val client = HttpClient(mockEngine)
        val api = TasksApi(client, "http://localhost", Dispatchers.Unconfined)

        val repository = DefaultTasksRepository(
            api = api,
            taskDao = database.taskDao,
            taskOutboxDao = database.taskOutboxDao,
            taskAssignmentDao = database.taskAssignmentDao,
            memberDao = database.memberDao,
            houseDao = database.houseDao,
            memberHouseAssociationDao = database.memberHouseAssociationDao,
            taskUploadRequestHandler = fakeTaskUploadRequestHandler,
        )

        val result = repository.editTask(
            taskId = "task1",
            name = "Updated Name",
            description = null,
            dueDateTime = null,
            repeatValue = null,
            repeatUnit = null,
            repeatEndDateTime = null,
            rotateMember = null,
            status = null
        )

        assertTrue(result is Result.Success)
        assertTrue(fakeTaskUploadRequestHandler.delayedUploadRequested)

        val outboxItems = database.taskOutboxDao.getOutboxItems()
        assertEquals(1, outboxItems.size)
        val updatedTask = outboxItems.first()
        assertEquals("Updated Name", updatedTask.name)
        assertEquals("edit", updatedTask.type)
        assertEquals("task1", updatedTask.taskId)
    }

    @Test
    fun testUploadLocalSuccessDeletesOutboxItems() = runTest {
        val outboxItem = TaskOutboxEntity(
            id = "outbox_1",
            type = "add",
            taskId = null,
            name = "New Task",
            description = "Desc",
            dueDateTime = LocalDateTime(2025, 1, 15, 10, 0),
            repeatValue = 1,
            repeatUnit = RepeatUnit.DAY,
            repeatEndDateTime = null,
            houseId = "house1",
            memberId = "member1",
            rotateMember = false,
            status = ActiveStatus.ACTIVE,
            createdDate = Clock.System.now()
        )
        database.taskOutboxDao.insert(outboxItem)

        val mockEngine = MockEngine { request ->
            assertEquals("/tasks/batch", request.url.encodedPath)
            respond(
                content = """{"successfulRequestIds":["outbox_1"],"deleteTaskAssignmentsTaskIds":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
        val api = TasksApi(client, "http://localhost", Dispatchers.Unconfined)

        val repository = DefaultTasksRepository(
            api = api,
            taskDao = database.taskDao,
            taskOutboxDao = database.taskOutboxDao,
            taskAssignmentDao = database.taskAssignmentDao,
            memberDao = database.memberDao,
            houseDao = database.houseDao,
            memberHouseAssociationDao = database.memberHouseAssociationDao,
            taskUploadRequestHandler = fakeTaskUploadRequestHandler,
        )

        val uploadedIds = repository.uploadLocal()
        assertEquals(1, uploadedIds.size)
        assertEquals("outbox_1", uploadedIds.first())

        val pendingAfter = database.taskOutboxDao.getOutboxItems()
        assertTrue(pendingAfter.isEmpty())
    }

    private class FakeTaskUploadRequestHandler : TaskUploadRequestHandler {
        var immediateUploadRequested = false
        var delayedUploadRequested = false

        override fun requestImmediateUpload(): Flow<Boolean> {
            immediateUploadRequested = true
            return flowOf(false)
        }

        override fun requestDelayedUpload() {
            delayedUploadRequested = true
        }

        override fun getUploadStatusFlow(): Flow<SyncStatus> {
            return flowOf(SyncStatus())
        }
    }
}
