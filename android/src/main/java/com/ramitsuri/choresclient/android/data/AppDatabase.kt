package com.ramitsuri.choresclient.android.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TaskAssignmentEntity::class,
        MemberEntity::class,
        TaskEntity::class,
        RequestCodeTimeAssociation::class,
        AssignmentTimeAssociation::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    ProgressStatusConverter::class,
    CreateTypeConverter::class,
    RepeatUnitConverter::class,
    InstantConverter::class
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun taskAssignmentDao(): TaskAssignmentDao
    abstract fun memberDao(): MemberDao
    abstract fun taskDao(): TaskDao
    abstract fun reminderAssignmentDao(): ReminderAssignmentDao
}
