package com.ramitsuri.choresclient.data.db

import com.ramitsuri.choresclient.data.ActiveStatus
import com.ramitsuri.choresclient.data.CreateType
import com.ramitsuri.choresclient.data.ProgressStatus
import com.ramitsuri.choresclient.data.RepeatUnit
import com.ramitsuri.choresclient.data.entities.AlarmDao
import com.ramitsuri.choresclient.data.entities.HouseDao
import com.ramitsuri.choresclient.data.entities.MemberDao
import com.ramitsuri.choresclient.data.entities.TaskAssignmentDao
import com.ramitsuri.choresclient.data.entities.TaskDao
import com.ramitsuri.choresclient.db.AlarmEntity
import com.ramitsuri.choresclient.db.ChoresDatabase
import com.ramitsuri.choresclient.db.HouseEntity
import com.ramitsuri.choresclient.db.MemberEntity
import com.ramitsuri.choresclient.db.TaskAssignmentEntity
import com.ramitsuri.choresclient.db.TaskEntity
import com.ramitsuri.choresclient.utils.DispatcherProvider
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

class Database(
    driver: SqlDriver,
    dispatcherProvider: DispatcherProvider
) {
    private val database = ChoresDatabase(
        driver = driver,
        TaskAssignmentEntityAdapter = TaskAssignmentEntity.Adapter(
            progressStatusAdapter = progressStatusConverter,
            progressStatusDateAdapter = instantConverter,
            dueDateTimeAdapter = localDateTimeConverter,
            createDateAdapter = instantConverter,
            createTypeAdapter = createTypeConverter
        ),
        AlarmEntityAdapter = AlarmEntity.Adapter(
            showAtTimeAdapter = localDateTimeConverter
        ),
        MemberEntityAdapter = MemberEntity.Adapter(
            createdDateAdapter = instantConverter
        ),
        TaskEntityAdapter = TaskEntity.Adapter(
            dueDateTimeAdapter = localDateTimeConverter,
            repeatUnitAdapter = repeatUnitConverter,
            createdDateAdapter = instantConverter,
            statusAdapter = statusConverter
        ),
        HouseEntityAdapter = HouseEntity.Adapter(
            createdDateAdapter = instantConverter,
            statusAdapter = statusConverter
        )
    )
    private val dbQuery = database.choresDatabaseQueries
    val taskAssignmentDao = TaskAssignmentDao(dbQuery, dispatcherProvider)
    val taskDao = TaskDao(dbQuery, dispatcherProvider)
    val memberDao = MemberDao(dbQuery, dispatcherProvider)
    val alarmDao = AlarmDao(dbQuery, dispatcherProvider)
    val houseDao = HouseDao(dbQuery, dispatcherProvider)
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

private val localDateTimeConverter = object : ColumnAdapter<LocalDateTime, String> {
    override fun decode(databaseValue: String): LocalDateTime {
        return LocalDateTime.parse(databaseValue)
    }

    override fun encode(value: LocalDateTime): String {
        return value.toString()
    }
}

private val statusConverter = object : ColumnAdapter<ActiveStatus, Long> {
    override fun decode(databaseValue: Long): ActiveStatus {
        return ActiveStatus.fromKey(databaseValue.toInt())
    }

    override fun encode(value: ActiveStatus): Long {
        return value.key.toLong()
    }
}