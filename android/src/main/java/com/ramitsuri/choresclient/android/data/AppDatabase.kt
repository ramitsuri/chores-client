package com.ramitsuri.choresclient.android.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec

@Database(
    entities = [
        TaskAssignmentEntity::class,
        MemberEntity::class,
        TaskEntity::class,
        AlarmEntity::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2,
            spec = AppDatabase.Migration1To2::class
        )
    ]
)
@TypeConverters(
    ProgressStatusConverter::class,
    CreateTypeConverter::class,
    RepeatUnitConverter::class,
    InstantConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskAssignmentDao(): TaskAssignmentDao
    abstract fun memberDao(): MemberDao
    abstract fun taskDao(): TaskDao
    abstract fun alarmDao(): AlarmDao

    @DeleteTable.Entries(
        DeleteTable(tableName = "AssignmentTimeAssociations"),
        DeleteTable(tableName = "RequestCodeTimeAssociations")
    )
    class Migration1To2 : AutoMigrationSpec
}
