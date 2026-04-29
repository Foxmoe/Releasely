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
import top.foxmoe.releasely.services.MedicationReminderManager
import top.foxmoe.releasely.services.SecuritySettingsService
import top.foxmoe.releasely.services.SyncMetaService
import top.foxmoe.releasely.services.SyncService
import top.foxmoe.releasely.services.WishlistService
import top.foxmoe.releasely.services.HealthReportService

class ReleaselyApp : Application() {
    lateinit var database: AppDatabase
    lateinit var profileQueries: ProfileQueries
    lateinit var activityService: ActivityService
    lateinit var cycleService: CycleService
    lateinit var medicationService: MedicationService
    lateinit var partnerService: PartnerService
    lateinit var apiService: ApiService
    lateinit var securitySettingsService: SecuritySettingsService

    // 延迟初始化非关键服务，减少冷启动时间
    val wishlistService by lazy { WishlistService(database) }
    val syncService by lazy {
        val syncMetaService = SyncMetaService(database)
        SyncService(apiService, syncMetaService, activityService, cycleService, medicationService, partnerService)
    }
    val reminderManager by lazy { MedicationReminderManager(applicationContext) }
    val healthReportService by lazy { HealthReportService(apiService) }

    override fun onCreate() {
        super.onCreate()
        val driver = AndroidSqliteDriver(AppDatabase.Schema, applicationContext, "releasely.db")
        database = AppDatabase(driver)
        profileQueries = database.profileQueries

        // 初始化核心服务
        activityService = ActivityService(database)
        cycleService = CycleService(database)
        medicationService = MedicationService(database)
        partnerService = PartnerService(database)
        apiService = ApiService()
        securitySettingsService = SecuritySettingsService(applicationContext, apiService)
    }

    /** 获取当前激活的用户资料，返回 null 表示未设置 */
    fun getActiveProfile(): top.foxmoe.releasely.database.Profile? {
        return try {
            profileQueries.getActiveProfile().executeAsOneOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /** 判断当前用户是否为女性（只有女性显示月经/周期相关功能） */
    fun isFemale(): Boolean {
        val profile = getActiveProfile()
        return profile?.gender == "Ms"
    }
}
