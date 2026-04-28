package top.foxmoe.releasely

import android.app.Application
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import top.foxmoe.releasely.database.AppDatabase
import top.foxmoe.releasely.database.ProfileQueries
import top.foxmoe.releasely.services.ActivityService
import top.foxmoe.releasely.services.CycleService
import top.foxmoe.releasely.services.MedicationService
import top.foxmoe.releasely.services.PartnerService
import top.foxmoe.releasely.services.ApiService
import top.foxmoe.releasely.services.SecuritySettingsService
import top.foxmoe.releasely.services.SyncService

class ReleaselyApp : Application() {
    lateinit var database: AppDatabase
    lateinit var profileQueries: ProfileQueries
    lateinit var activityService: ActivityService
    lateinit var cycleService: CycleService
    lateinit var medicationService: MedicationService
    lateinit var partnerService: PartnerService
    lateinit var apiService: ApiService
    lateinit var syncService: SyncService
    lateinit var securitySettingsService: SecuritySettingsService

    override fun onCreate() {
        super.onCreate()
        val driver = AndroidSqliteDriver(AppDatabase.Schema, applicationContext, "releasely.db")
        database = AppDatabase(driver)
        profileQueries = database.profileQueries

        // Initialize services
        activityService = ActivityService(database)
        cycleService = CycleService(database)
        medicationService = MedicationService(database)
        partnerService = PartnerService(database)
        apiService = ApiService()
        syncService = SyncService(apiService, activityService, cycleService, medicationService, partnerService)
        securitySettingsService = SecuritySettingsService(applicationContext, apiService)
    }
}

