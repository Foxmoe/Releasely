package top.foxmoe.releasely

import android.app.Application
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import top.foxmoe.releasely.database.AppDatabase
import top.foxmoe.releasely.database.ProfileQueries

class ReleaselyApp : Application() {
    lateinit var database: AppDatabase
    lateinit var profileQueries: ProfileQueries

    override fun onCreate() {
        super.onCreate()
        val driver = AndroidSqliteDriver(AppDatabase.Schema, applicationContext, "releasely.db")
        database = AppDatabase(driver)
        profileQueries = database.profileQueries
    }
}

