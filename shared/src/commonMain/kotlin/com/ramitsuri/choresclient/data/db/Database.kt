package com.ramitsuri.choresclient.data.db

import com.ramitsuri.choresclient.data.CreateType
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.entities.AlarmDao
import com.ramitsuri.choresclient.data.entities.MemberDao
import com.ramitsuri.choresclient.data.entities.TaskAssignmentDao
import com.ramitsuri.choresclient.data.entities.TaskDao
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.squareup.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

class Database(
    databaseDriverFactory: DatabaseDriverFactory,
    dispatcherProvider: DispatcherProvider
) {
    private val database = ChoresDatabase(
        driver = databaseDriverFactory.createDriver(),
        TaskAssignmentEntityAdapter = TaskAssignmentEntity.Adapter(
            progressStatusAdapter = progressStatusConverter,
            progressStatusDateAdapter = instantConverter,
            dueDateTimeAdapter = instantConverter,
            createDateAdapter = instantConverter,
            createTypeAdapter = createTypeConverter
        ),
        AlarmEntityAdapter = AlarmEntity.Adapter(
            showAtTimeAdapter = instantConverter
        ),
        MemberEntityAdapter = MemberEntity.Adapter(
            createdDateAdapter = instantConverter
        ),
        TaskEntityAdapter = TaskEntity.Adapter(
            dueDateTimeAdapter = instantConverter,
            repeatUnitAdapter = repeatUnitConverter,
            createdDateAdapter = instantConverter
        )

    )
    private val dbQuery = database.choresDatabaseQueries
    val taskAssignmentDao = TaskAssignmentDao(dbQuery, dispatcherProvider)
    val taskDao = TaskDao(dbQuery, dispatcherProvider)
    val memberDao = MemberDao(dbQuery, dispatcherProvider)
    val alarmDao = AlarmDao(dbQuery, dispatcherProvider)
}

private val progressStatusConverter = object : ColumnAdapter<ProgressStatus, Long> {
    override fun decode(databaseValue: Long): ProgressStatus {
        return ProgressStatus.fromKey(databaseValue.toInt())
    }

    override fun encode(value: ProgressStatus): Long {
        return value.key.toLong()
    }
}

private val createTypeConverter = object : ColumnAdapter<CreateType, Long> {
    override fun decode(databaseValue: Long): CreateType {
        return CreateType.fromKey(databaseValue.toInt())
    }

    override fun encode(value: CreateType): Long {
        return value.key.toLong()
    }
}

private val repeatUnitConverter = object : ColumnAdapter<RepeatUnit, Long> {
    override fun decode(databaseValue: Long): RepeatUnit {
        return RepeatUnit.fromKey(databaseValue.toInt())
    }

    override fun encode(value: RepeatUnit): Long {
        return value.key.toLong()
    }
}

private val instantConverter = object : ColumnAdapter<Instant, Long> {
    override fun decode(databaseValue: Long): Instant {
        return Instant.fromEpochMilliseconds(databaseValue)
    }

    override fun encode(value: Instant): Long {
        return value.toEpochMilliseconds()
    }
}