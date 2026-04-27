package top.foxmoe.releasely.shared.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import top.foxmoe.releasely.shared.db.ReleaselyDatabase

actual class DatabaseDriverFactory(
    private val context: Context
) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = ReleaselyDatabase.Schema,
            context = context,
            name = "releasely-shared.db"
        )
    }
}
