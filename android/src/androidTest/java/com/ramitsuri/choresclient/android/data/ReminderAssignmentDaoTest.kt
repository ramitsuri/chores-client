package com.ramitsuri.choresclient.android.data

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import junit.framework.Assert.fail
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderAssignmentDaoTest: BaseDaoTest() {
    private lateinit var dao: ReminderAssignmentDao

    @Before
    fun setup() {
        dao = db.reminderAssignmentDao()
    }

    @Test
    fun testInsertRequestCodeTimeAssociation_shouldInsert_ifNotExists() {
        runBlocking {
            dao.insertRequestCodeTimeAssociation(RequestCodeTimeAssociation(time = 1))
            val reminder = dao.getRequestCodeTimeAssociation(1)
            assertNotNull(reminder)
            assertTrue(reminder!!.requestCode != 0)
        }
    }

    @Test
    fun testInsertRequestCodeTimeAssociation_shouldNotInsert_ifExists() {
        runBlocking {
            var sqlConstraintExceptionCaught = false
            dao.insertRequestCodeTimeAssociation(RequestCodeTimeAssociation(time = 1))
            try {
                dao.insertRequestCodeTimeAssociation(RequestCodeTimeAssociation(time = 1))
                fail()
            } catch (e: SQLiteConstraintException) {
                sqlConstraintExceptionCaught = true
            }
            assertTrue(sqlConstraintExceptionCaught)
        }
    }

    @Test
    fun testInsertAssignmentTimeAssociation_shouldInsert_ifNotExists() {
        runBlocking {
            dao.insertAssignmentTimeAssociation(
                AssignmentTimeAssociation(
                    assignmentId = "1",
                    time = 1
                )
            )
            val association = dao.getAssignmentTimeAssociation("1")
            assertNotNull(association)
        }
    }

    @Test
    fun testInsertAssignmentTimeAssociation_shouldReplace_ifExists() {
        runBlocking {
            dao.insertAssignmentTimeAssociation(
                AssignmentTimeAssociation(
                    assignmentId = "1",
                    time = 1
                )
            )
            dao.insertAssignmentTimeAssociation(
                AssignmentTimeAssociation(
                    assignmentId = "1",
                    time = 2
                )
            )
            assertEquals(1, dao.getAssignmentTimeAssociations().size)
            assertEquals(2, dao.getAssignmentTimeAssociations()[0].time)
        }
    }

    @Test
    fun testInsertTransaction() {
        runBlocking {
            val insertResult = dao.insert(assignmentId = "1", assignmentTimeMillis = 1)
            assertEquals(
                ReminderAssignment(assignmentId = "1", time = 1, requestCode = 1),
                insertResult
            )
        }
    }

    @Test
    fun testInsertTransaction_shouldNotInsertReminderAgain_ifAlreadyExistsForTime() {
        runBlocking {
            dao.insert(assignmentId = "1", assignmentTimeMillis = 1)
            dao.insert(assignmentId = "2", assignmentTimeMillis = 1)
            assertEquals(2, dao.getAssignmentTimeAssociations().size)
            assertEquals(1, dao.getRequestCodeTimeAssociations().size)
        }
    }

    @Test
    fun testGetTransaction_shouldReturnNonNull_ifExists() {
        runBlocking {
            dao.insert(assignmentId = "1", assignmentTimeMillis = 1)
            assertEquals(
                ReminderAssignment(assignmentId = "1", time = 1, requestCode = 1),
                dao.get("1")
            )
        }
    }

    @Test
    fun testGetTransaction_shouldReturnNull_ifAssociationNotExists() {
        runBlocking {
            dao.insert(assignmentId = "1", assignmentTimeMillis = 1)
            dao.deleteAssignmentTimeAssociation("1")
            assertEquals(
                null,
                dao.get("1")
            )
        }
    }

    @Test
    fun testGetTransaction_shouldReturnNull_ifReminderNotExists() {
        runBlocking {
            dao.insert(assignmentId = "1", assignmentTimeMillis = 1)
            dao.deleteRequestCodeTimeAssociation(1)
            assertEquals(
                null,
                dao.get("1")
            )
        }
    }

    @Test
    fun testUpdateOrInsert_ifOtherAssociationsForOldTimeExist_andForNewTimeNotExist() {
        val oldTime = 1L
        val newTime = 2L
        runBlocking {
            dao.insert(assignmentId = "1", assignmentTimeMillis = oldTime)
            dao.insert(assignmentId = "2", assignmentTimeMillis = oldTime)
            dao.insert(assignmentId = "3", assignmentTimeMillis = oldTime)
            /**
             * BEFORE                   AFTER
             *
             * REQ   |   TIME           REQ   |   TIME
             * 1     |   1              1     |   1
             *                          2     |   2
             *
             * ASS_ID |  TIME           ASS_ID |  TIME
             * 1      |  1              1      |  2
             * 2      |  1              2      |  1
             * 3      |  1              3      |  1
             */
            val updateResult = dao.updateOrInsert("1", newTime, oldTime)
            assertEquals(
                ReminderAssignmentUpdateResult(
                    reminderAssignment = ReminderAssignment(
                        assignmentId = "1",
                        time = 2,
                        requestCode = 2
                    ),
                    oldTimeNoLongerExists = false
                ), updateResult
            )
        }
    }

    @Test
    fun testUpdateOrInsert_ifOtherAssociationsForOldTimeNotExist_andForNewTimeNotExist() {
        val oldTime = 1L
        val newTime = 2L
        runBlocking {
            dao.insert(assignmentId = "1", assignmentTimeMillis = oldTime)
            /**
             * BEFORE                   AFTER
             *
             * REQ   |   TIME           REQ   |   TIME
             * 1     |   1              2     |   2
             *
             * ASS_ID |  TIME           ASS_ID |  TIME
             * 1      |  1              1      |  2
             */
            val updateResult = dao.updateOrInsert("1", newTime, oldTime)
            assertEquals(
                ReminderAssignmentUpdateResult(
                    reminderAssignment = ReminderAssignment(
                        assignmentId = "1",
                        time = 2,
                        requestCode = 2
                    ),
                    oldTimeNoLongerExists = true
                ), updateResult
            )
        }
    }

    @Test
    fun testUpdateOrInsert_ifOtherAssociationsForOldTimeNotExist_andForNewTimeExist() {
        val oldTime = 1L
        val newTime = 2L
        runBlocking {
            dao.insert(assignmentId = "1", assignmentTimeMillis = oldTime)
            dao.insert(assignmentId = "2", assignmentTimeMillis = newTime)
            dao.insert(assignmentId = "3", assignmentTimeMillis = newTime)
            dao.insert(assignmentId = "4", assignmentTimeMillis = newTime)
            /**
             * BEFORE                   AFTER
             *
             * REQ   |   TIME           REQ   |   TIME
             * 1     |   1              2     |   2
             * 2     |   2
             *
             * ASS_ID |  TIME           ASS_ID |  TIME
             * 1      |  1              1      |  2
             * 2      |  2              2      |  2
             * 3      |  2              3      |  2
             * 4      |  2              3      |  2
             */
            val updateResult = dao.updateOrInsert("1", newTime, oldTime)
            assertEquals(
                ReminderAssignmentUpdateResult(
                    reminderAssignment = ReminderAssignment(
                        assignmentId = "1",
                        time = 2,
                        requestCode = 2
                    ),
                    oldTimeNoLongerExists = true
                ), updateResult
            )
        }
    }

    @Test
    fun testUpdateOrInsert_ifOtherAssociationsForOldTimeExist_andForNewTimeExist() {
        val oldTime = 1L
        val newTime = 2L
        runBlocking {
            dao.insert(assignmentId = "1", assignmentTimeMillis = oldTime)
            dao.insert(assignmentId = "2", assignmentTimeMillis = oldTime)
            dao.insert(assignmentId = "3", assignmentTimeMillis = oldTime)
            dao.insert(assignmentId = "4", assignmentTimeMillis = newTime)
            /**
             * BEFORE                   AFTER
             *
             * REQ   |   TIME           REQ   |   TIME
             * 1     |   1              1     |   1
             * 2     |   2              2     |   2
             *
             * ASS_ID |  TIME           ASS_ID |  TIME
             * 1      |  1              1      |  2
             * 2      |  1              2      |  1
             * 3      |  1              3      |  1
             * 4      |  2              4      |  2
             */
            val updateResult = dao.updateOrInsert("1", newTime, oldTime)
            assertEquals(
                ReminderAssignmentUpdateResult(
                    reminderAssignment = ReminderAssignment(
                        assignmentId = "1",
                        time = 2,
                        requestCode = 2
                    ),
                    oldTimeNoLongerExists = false
                ), updateResult
            )
        }
    }
}