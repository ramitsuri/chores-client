package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.network.model.TaskBatchDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TasksApiTest {

    @Test
    fun testGetTasksSuccess() = runTest {
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
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val api = TasksApi(client, "http://localhost", Dispatchers.Unconfined)
        val result = api.getTasks()

        assertTrue(result is Result.Success)
        val tasks = (result as Result.Success).data
        assertEquals(1, tasks.size)
        assertEquals("task1", tasks[0].id)
        assertEquals("Clean Kitchen", tasks[0].name)
    }

    @Test
    fun testBatchTasksSuccess() = runTest {
        val responseJson = """
            {
                "successfulRequestIds": ["req1", "req2"],
                "deleteTaskAssignmentsTaskIds": ["task2"]
            }
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            assertEquals("/tasks/batch", request.url.encodedPath)
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = TasksApi(client, "http://localhost", Dispatchers.Unconfined)

        val batchRequests = listOf(
            TaskBatchDto.Add(
                name = "Task 1",
                description = "Desc 1",
                dueDateTime = LocalDateTime(2025, 1, 15, 10, 0),
                houseId = "house1",
                memberId = "member1",
                clientRequestId = "req1"
            ),
            TaskBatchDto.Edit(
                taskId = "task2",
                clientRequestId = "req2",
                name = "Task 2 Updated"
            )
        )

        val result = api.batchTasks(batchRequests)
        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(2, data.successfulRequestIds.size)
        assertEquals("req1", data.successfulRequestIds[0])
        assertEquals("req2", data.successfulRequestIds[1])
        assertEquals(1, data.deleteTaskAssignmentsTaskIds.size)
        assertEquals("task2", data.deleteTaskAssignmentsTaskIds[0])
    }
}
