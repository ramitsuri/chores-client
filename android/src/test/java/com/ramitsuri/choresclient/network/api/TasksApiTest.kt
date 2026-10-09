package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
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
                    "description": "Clean counters and sink",
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
        val data = (result as Result.Success).data
        assertEquals(1, data.size)
        assertEquals("task1", data.first().id)
        assertEquals("Clean Kitchen", data.first().name)
        assertTrue(data.first().rotateMember)
    }
}
