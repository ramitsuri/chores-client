package com.ramitsuri.choresclient.network.api

import com.ramitsuri.choresclient.model.Result
import com.ramitsuri.choresclient.model.entities.RepeatInfo
import com.ramitsuri.choresclient.model.entities.TaskAssignment
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.model.enums.RepeatUnit
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
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
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
                    "progressStatus": "1",
                    "progressStatusDate": "2023-10-01T10:00:00Z",
                    "task": {
                        "id": "task1",
                        "name": "Clean kitchen",
                        "description": "Clean counters and sink",
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
            assertEquals("/task-assignments/filter?progress=1", request.url.toString().removePrefix("http://localhost"))
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
        val data = (result as Result.Success).data
        assertEquals(1, data.size)
        assertEquals("assignment1", data.first().id)
        assertEquals("Clean kitchen", data.first().task.name)
        assertEquals("Ramit", data.first().member.name)
    }

    @Test
    fun testGetTaskAssignmentsWithMultipleProgressStatuses() = runTest {
        val mockEngine = MockEngine { request ->
            val progressValues = request.url.parameters.getAll("progress")
            assertTrue(progressValues!!.contains("3,4"))
            respond(
                content = "[]",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockHttpClient(mockEngine)
        val api = TaskAssignmentsApi(client, "http://localhost", Dispatchers.Unconfined)

        val result = api.getTaskAssignments(listOf(ProgressStatus.DONE, ProgressStatus.WONT_DO))
        assertTrue(result is Result.Success)
    }

    @Test
    fun testGetTaskAssignmentsFailure() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }

        val client = createMockHttpClient(mockEngine)
        val api = TaskAssignmentsApi(client, "http://localhost", Dispatchers.Unconfined)

        val result = api.getTaskAssignments()
        assertTrue(result is Result.Failure)
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

        val assignmentsToUpdate = listOf(
            TaskAssignment(
                id = "assignment1",
                progressStatus = ProgressStatus.DONE,
                progressStatusDate = Instant.fromEpochMilliseconds(2000L),
                taskId = "task1",
                taskName = "Clean kitchen",
                houseId = "house1",
                repeatInfo = RepeatInfo(1, RepeatUnit.DAY, null),
                memberId = "member1",
                memberName = "Ramit",
                dueDateTime = LocalDateTime.parse("2023-10-01T10:00:00")
            )
        )

        val result = api.updateTaskAssignments(assignmentsToUpdate)
        assertTrue(result is Result.Success)
        val uploadedIds = (result as Result.Success).data
        assertEquals(listOf("assignment1", "assignment2"), uploadedIds)
    }

    private fun createMockHttpClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
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
    }
}
