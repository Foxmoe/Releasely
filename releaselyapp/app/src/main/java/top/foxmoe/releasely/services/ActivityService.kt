package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

class ActivityService(private val queries: top.foxmoe.releasely.database.AppDatabaseQueries) {

    suspend fun getAllActivities(): List<ActivityRecord> = withContext(Dispatchers.IO) {
        queries.getAllActivities().executeAsList().map { row ->
            ActivityRecord(
                id = row.id,
                date = row.date,
                type = row.type,
                protection = row.protection == 1L,
                pleasure = row.pleasure?.toInt(),
                mood = row.mood,
                notes = row.notes,
                partnerId = row.partnerId,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun getActivityById(id: String): ActivityRecord? = withContext(Dispatchers.IO) {
        queries.getActivityById(id).executeAsOneOrNull()?.let { row ->
            ActivityRecord(
                id = row.id,
                date = row.date,
                type = row.type,
                protection = row.protection == 1L,
                pleasure = row.pleasure?.toInt(),
                mood = row.mood,
                notes = row.notes,
                partnerId = row.partnerId,
                createdAt = row.createdAt
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
        queries.insertActivity(
            id = id,
            date = date,
            type = type,
            protection = if (protection) 1L else 0L,
            pleasure = pleasure?.toLong(),
            mood = mood,
            notes = notes,
            partnerId = partnerId
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
        queries.updateActivity(
            date = date,
            type = type,
            protection = if (protection) 1L else 0L,
            pleasure = pleasure?.toLong(),
            mood = mood,
            notes = notes,
            partnerId = partnerId,
            id = id
        )
    }

    suspend fun deleteActivity(id: String) = withContext(Dispatchers.IO) {
        queries.deleteActivity(id)
    }

    suspend fun getActivitiesByDateRange(startDate: Long, endDate: Long): List<ActivityRecord> =
        withContext(Dispatchers.IO) {
            queries.getActivitiesByDateRange(startDate, endDate).executeAsList().map { row ->
                ActivityRecord(
                    id = row.id,
                    date = row.date,
                    type = row.type,
                    protection = row.protection == 1L,
                    pleasure = row.pleasure?.toInt(),
                    mood = row.mood,
                    notes = row.notes,
                    partnerId = row.partnerId,
                    createdAt = row.createdAt
                )
            }
        }

    suspend fun getActivityCount(): Long = withContext(Dispatchers.IO) {
        queries.countActivities().executeAsOne()
    }
}