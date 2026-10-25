package com.ramitsuri.choresclient.data.db.dao

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ramitsuri.choresclient.data.db.Database
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.model.entities.TaskAssignmentUpdate
import com.ramitsuri.choresclient.model.enums.CreateType
import com.ramitsuri.choresclient.model.enums.ProgressStatus
import com.ramitsuri.choresclient.utils.DispatcherProvider
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime

class TaskAssignmentDaoTest {

    private lateinit var database: Database
    private lateinit var dao: TaskAssignmentDao

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ChoresDatabase.Schema.create(driver)
        val testDispatcher = UnconfinedTestDispatcher()
        val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, testDispatcher)
        database = Database(driver, dispatcherProvider)
        dao = database.taskAssignmentDao
    }

    @Test
    fun testInsertAndGetAll() = runTest {
        val entity1 = createEntity("id1", ProgressStatus.TODO, false)
        val entity2 = createEntity("id2", ProgressStatus.DONE, false)

        dao.clearTodoAndInsert(listOf(entity1, entity2))

        val all = dao.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun testGetById() = runTest {
        val entity = createEntity("id1", ProgressStatus.TODO, false)
        dao.clearTodoAndInsert(listOf(entity))

        val fetched = dao.get("id1")
        assertNotNull(fetched)
        assertEquals("id1", fetched?.id)

        val nonExistent = dao.get("unknown")
        assertNull(nonExistent)
    }

    @Test
    fun testGetForUpload() = runTest {
        val forUpload = createEntity("id1", ProgressStatus.DONE, shouldUpload = true)
        val notForUpload = createEntity("id2", ProgressStatus.TODO, shouldUpload = false)

        dao.clearTodoAndInsert(listOf(forUpload, notForUpload))

        val uploadList = dao.getForUpload()
        assertEquals(1, uploadList.size)
        assertEquals("id1", uploadList.first().id)
    }

    @Test
    fun testUpdate() = runTest {
        val entity = createEntity("id1", ProgressStatus.TODO, shouldUpload = false)
        dao.clearTodoAndInsert(listOf(entity))

        val updateTime = Instant.fromEpochMilliseconds(2000L)
        val update = TaskAssignmentUpdate(
            id = "id1",
            progressStatus = ProgressStatus.DONE,
            progressStatusDate = updateTime,
            shouldUpload = true
        )
        dao.update(update)

        val updated = dao.get("id1")
        assertNotNull(updated)
        assertEquals(ProgressStatus.DONE, updated?.progressStatus)
        assertEquals(updateTime, updated?.progressStatusDate)
        assertTrue(updated?.shouldUpload == true)
    }

    @Test
    fun testClearTodoAndInsert() = runTest {
        val oldTodo = createEntity("id1", ProgressStatus.TODO, false)
        dao.clearTodoAndInsert(listOf(oldTodo))

        val newTodo = createEntity("id2", ProgressStatus.TODO, false)
        dao.clearTodoAndInsert(listOf(newTodo))

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("id2", all.first().id)
    }

    @Test
    fun testDeleteByIds() = runTest {
        val entity1 = createEntity("id1", ProgressStatus.TODO, false)
        val entity2 = createEntity("id2", ProgressStatus.TODO, false)
        dao.clearTodoAndInsert(listOf(entity1, entity2))

        dao.delete(listOf("id1"))

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("id2", all.first().id)
    }

    @Test
    fun testDeleteOldCompleted() = runTest {
        val oldUploadedCompleted = createEntity("id1", ProgressStatus.DONE, shouldUpload = false, date = 1000L)
        val oldUnuploadedCompleted = createEntity("id2", ProgressStatus.DONE, shouldUpload = true, date = 1000L)
        val recentCompleted = createEntity("id3", ProgressStatus.DONE, shouldUpload = false, date = 3000L)
        val oldTodo = createEntity("id4", ProgressStatus.TODO, shouldUpload = false, date = 1000L)

        dao.insert(listOf(oldUploadedCompleted, oldUnuploadedCompleted, recentCompleted, oldTodo))

        val cutoff = Instant.fromEpochMilliseconds(2000L)
        dao.deleteOldCompleted(cutoff)

        val all = dao.getAll()
        assertEquals(3, all.size)
        val ids = all.map { it.id }.toSet()
        assertTrue(ids.contains("id2")) // kept because shouldUpload = true
        assertTrue(ids.contains("id3")) // kept because recent
        assertTrue(ids.contains("id4")) // kept because TODO
        assertFalse(ids.contains("id1")) // deleted because old & uploaded
    }

    private fun createEntity(
        id: String,
        progressStatus: ProgressStatus,
        shouldUpload: Boolean,
        shouldTransfer: Boolean = false,
        date: Long = 1000L
    ): TaskAssignmentEntity {
        return TaskAssignmentEntity(
            id = id,
            progressStatus = progressStatus,
            progressStatusDate = Instant.fromEpochMilliseconds(date),
            taskId = "taskId1",
            memberId = "memberId1",
            dueDateTime = LocalDateTime.parse("2023-10-01T10:00:00"),
            createDate = Instant.fromEpochMilliseconds(500L),
            createType = CreateType.AUTO,
            shouldUpload = shouldUpload,
            shouldTransfer = shouldTransfer
        )
    }
}
