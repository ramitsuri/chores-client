package com.ramitsuri.choresclient.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.db.HouseEntity
import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.enums.ActiveStatus
import com.ramitsuri.choresclient.network.api.TasksApi
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.ramitsuri.choresclient.utils.LogHelper
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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

class TasksRepositoryTest {

    private lateinit var database: Database

    @Before
    fun setUp() {
        startKoin {
            modules(module {
                single { LogHelper(enableLocal = false, enableRemote = false, deviceDetails = "test") }
            })
        }

        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ChoresDatabase.Schema.create(driver)
        val dispatcherProvider = DispatcherProvider()
        database = Database(driver, dispatcherProvider)
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
            assertEquals("/tasks", request.url.encodedPath)
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val api = TasksApi(client, "http://localhost", Dispatchers.Unconfined)

        val repository = DefaultTasksRepository(
            api = api,
            taskDao = database.taskDao,
            memberDao = database.memberDao,
            houseDao = database.houseDao
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

        val mockEngine = MockEngine { respond("", HttpStatusCode.OK) }
        val client = HttpClient(mockEngine)
        val api = TasksApi(client, "http://localhost", Dispatchers.Unconfined)

        val repository = DefaultTasksRepository(
            api = api,
            taskDao = database.taskDao,
            memberDao = database.memberDao,
            houseDao = database.houseDao
        )

        // Insert task directly via DAO
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
            HttpClient(MockEngine {
                respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } },
            "http://localhost",
            Dispatchers.Unconfined
        )
        val fetchRepo = DefaultTasksRepository(fetchApi, database.taskDao, database.memberDao, database.houseDao)
        fetchRepo.fetchTasks()

        val taskDetailsList = repository.getTasksFlow().first()
        assertEquals(1, taskDetailsList.size)
        val details = taskDetailsList.first()
        assertEquals("task1", details.task.id)
        assertEquals("Ramit", details.memberName)
        assertEquals("Home", details.houseName)
    }
}
