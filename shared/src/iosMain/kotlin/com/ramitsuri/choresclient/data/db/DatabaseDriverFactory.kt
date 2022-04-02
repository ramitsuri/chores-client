package com.ramitsuri.choresclient.data.db

import com.ramitsuri.choresclient.db.ChoresDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(ChoresDatabase.Schema, "chores.db")
    }
}