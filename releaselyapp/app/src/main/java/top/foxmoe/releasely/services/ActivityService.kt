package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.foxmoe.releasely.security.LocalEncryptionManager
import java.util.UUID

data class ActivityRecord(
    val id: String,
    val date: Long,
    val type: String,
    val protection: Boolean,
    val pleasure: Int?,
    val mood: String?,
    val notes: String?,
    val partnerId: String?,
    val createdAt: Long
)

class ActivityService(private val database: top.foxmoe.releasely.database.AppDatabase) {

    suspend fun getAllActivities(): List<ActivityRecord> = withContext(Dispatchers.IO) {
        database.activityQueries.getAllActivities().executeAsList().map { row ->
            ActivityRecord(
                id = row.id,
                date = row.date,
                type = row.type,
                protection = row.protection == 1L,
                pleasure = row.pleasure?.toInt(),
                mood = row.mood,
                notes = row.notes?.let { decryptIfNeeded(it) },
                partnerId = row.partner_id,
                createdAt = row.created_at
            )
        }
    }

    suspend fun getActivityById(id: String): ActivityRecord? = withContext(Dispatchers.IO) {
        database.activityQueries.getActivityById(id).executeAsOneOrNull()?.let { row ->
            ActivityRecord(
                id = row.id,
                date = row.date,
                type = row.type,
                protection = row.protection == 1L,
                pleasure = row.pleasure?.toInt(),
                mood = row.mood,
                notes = row.notes?.let { decryptIfNeeded(it) },
                partnerId = row.partner_id,
                createdAt = row.created_at
            )
        }
    }

    suspend fun insertActivity(
        date: Long,
        type: String,
        protection: Boolean,
        pleasure: Int?,
        mood: String?,
        notes: String?,
        partnerId: String?
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        database.activityQueries.insertActivity(
            id = id,
            date = date,
            type = type,
            protection = if (protection) 1L else 0L,
            pleasure = pleasure?.toLong(),
            mood = mood,
            notes = notes?.let { LocalEncryptionManager.encrypt(it) },
            partner_id = partnerId
        )
        id
    }

    suspend fun updateActivity(
        id: String,
        date: Long,
        type: String,
        protection: Boolean,
        pleasure: Int?,
        mood: String?,
        notes: String?,
        partnerId: String?
    ) = withContext(Dispatchers.IO) {
        database.activityQueries.updateActivity(
            date = date,
            type = type,
            protection = if (protection) 1L else 0L,
            pleasure = pleasure?.toLong(),
            mood = mood,
            notes = notes?.let { LocalEncryptionManager.encrypt(it) },
            partner_id = partnerId,
            id = id
        )
    }

    suspend fun deleteActivity(id: String) = withContext(Dispatchers.IO) {
        database.activityQueries.deleteActivity(id)
    }

    suspend fun getActivitiesByDateRange(startDate: Long, endDate: Long): List<ActivityRecord> =
        withContext(Dispatchers.IO) {
            database.activityQueries.getActivitiesByDateRange(startDate, endDate).executeAsList().map { row ->
                ActivityRecord(
                    id = row.id,
                    date = row.date,
                    type = row.type,
                    protection = row.protection == 1L,
                    pleasure = row.pleasure?.toInt(),
                    mood = row.mood,
                    notes = row.notes?.let { decryptIfNeeded(it) },
                    partnerId = row.partner_id,
                    createdAt = row.created_at
                )
            }
        }

    suspend fun getActivityCount(): Long = withContext(Dispatchers.IO) {
        database.activityQueries.countActivities().executeAsOne()
    }

    private fun decryptIfNeeded(value: String): String {
        return try {
            LocalEncryptionManager.decrypt(value)
        } catch (e: Exception) {
            value
        }
    }
}
