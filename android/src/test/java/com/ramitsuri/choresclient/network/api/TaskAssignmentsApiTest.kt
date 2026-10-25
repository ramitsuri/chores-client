package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.TaskAssignmentUpdate
import com.ramitsuri.choresclient.model.enums.ProgressStatus
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
import kotlin.time.Instant

class TaskAssignmentsApiTest {

    @Test
    fun testGetTaskAssignmentsSuccess() = runTest {
        val responseJson = """
            [
                {
                    "id": "assignment1",
                    "progressStatus": 1,
                    "progressStatusDate": "2023-10-01T10:00:00Z",
                    "task": {
                        "id": "task1",
                        "name": "Clean kitchen",
                        "description": "Wipe counters",
                        "dueDateTime": "2023-10-01T10:00:00",
                        "repeatValue": 1,
                        "repeatUnit": "1",
                        "repeatEndDateTime": null,
                        "houseId": "house1",
                        "memberId": "member1",
                        "rotateMember": false,
                        "createdDate": "2023-09-01T10:00:00Z",
                        "status": 1
                    },
                    "member": {
                        "id": "member1",
                        "name": "Ramit"
                    },
                    "dueDateTime": "2023-10-01T10:00:00",
                    "createdDate": "2023-09-01T10:00:00Z",
                    "createType": 1
                }
            ]
        """.trimIndent()

        val mockEngine = MockEngine { request ->
            assertEquals("/task-assignments/filter", request.url.encodedPath)
            assertEquals("1", request.url.parameters["progress"])
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockHttpClient(mockEngine)
        val api = TaskAssignmentsApi(client, "http://localhost", Dispatchers.Unconfined)
        val result = api.getTaskAssignments()

        assertTrue(result is Result.Success)
        val assignments = (result as Result.Success).data
        assertEquals(1, assignments.size)
        assertEquals("assignment1", assignments[0].id)
    }

    @Test
    fun testUpdateTaskAssignmentsSuccess() = runTest {
        val responseJson = """["assignment1", "assignment2"]"""

        val mockEngine = MockEngine { request ->
            assertEquals("/task-assignments", request.url.encodedPath)
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockHttpClient(mockEngine)
        val api = TaskAssignmentsApi(client, "http://localhost", Dispatchers.Unconfined)

        val updatesToSubmit = listOf(
            TaskAssignmentUpdate(
                id = "assignment1",
                progressStatus = ProgressStatus.DONE,
                progressStatusDate = Instant.fromEpochMilliseconds(2000L),
                shouldUpload = true,
                shouldTransfer = false
            ),
            TaskAssignmentUpdate(
                id = "assignment2",
                progressStatus = ProgressStatus.WONT_DO,
                progressStatusDate = Instant.fromEpochMilliseconds(2000L),
                shouldUpload = true,
                shouldTransfer = true
            )
        )

        val result = api.updateTaskAssignments(updatesToSubmit)
        assertTrue(result is Result.Success)
        val uploadedIds = (result as Result.Success).data
        assertEquals(listOf("assignment1", "assignment2"), uploadedIds)
    }

    private fun createMockHttpClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
