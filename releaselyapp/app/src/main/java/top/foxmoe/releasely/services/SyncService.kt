package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId

data class SyncRecord(
    val id: String,
    val tableName: String,
    val recordId: String,
    val operation: String,
    val data: String,
    val timestamp: Long,
    val synced: Boolean
)

class SyncService(
    private val apiService: ApiService,
    private val activityService: ActivityService,
    private val cycleService: CycleService,
    private val medicationService: MedicationService,
    private val partnerService: PartnerService
) {
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    suspend fun syncAll(userId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            pushActivities(userId)
            pushCycles(userId)
            pushMedications(userId)
            pushPartners(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun pushActivities(userId: Long) {
        val activities = activityService.getAllActivities()
        activities.forEach { activity ->
            val json = JSONObject().apply {
                put("userId", userId)
                put("type", activity.type)
                put("protection", if (activity.protection) "有保护" else "无保护")
                put("pleasureRating", activity.pleasure ?: 0)
                put("healthStatus", activity.mood ?: "")
                put("occurredAt", Instant.ofEpochSecond(activity.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime().toString())
                put("notes", activity.notes ?: "")
            }
            apiService.post("/activity", json.toString(), authToken)
        }
    }

    private suspend fun pushCycles(userId: Long) {
        val cycles = cycleService.getAllCycles()
        cycles.forEach { cycle ->
            val json = JSONObject().apply {
                put("userId", userId)
                put("startDate", Instant.ofEpochSecond(cycle.startDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().toString())
                put("duration", cycle.duration ?: 5)
                put("predictedNext", cycle.predictedNext?.let {
                    Instant.ofEpochSecond(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().toString()
                })
            }
            apiService.post("/cycles", json.toString(), authToken)
        }
    }

    private suspend fun pushMedications(userId: Long) {
        val medications = medicationService.getAllMedications()
        medications.forEach { medication ->
            val json = JSONObject().apply {
                put("userId", userId)
                put("name", medication.name)
                put("dosage", medication.dosage)
                put("reminderTime", Instant.ofEpochSecond(medication.reminderTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime().toString())
            }
            apiService.post("/medications", json.toString(), authToken)
        }
    }

    private suspend fun pushPartners(userId: Long) {
        val partners = partnerService.getAllPartners()
        partners.forEach { partner ->
            val json = JSONObject().apply {
                put("userId", userId)
                put("partnerId", partner.id)
                put("status", partner.status)
                put("sharedPermissions", partner.inviteCode)
            }
            apiService.post("/partners", json.toString(), authToken)
        }
    }

    suspend fun fetchFromServer(userId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            fetchActivities(userId)
            fetchCycles(userId)
            fetchMedications(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchActivities(userId: Long) {
        val response = apiService.get("/activity/list?userId=$userId", authToken)
        response.getOrNull()?.let { jsonString ->
            val jsonObj = JSONObject(jsonString)
            val data = jsonObj.optJSONObject("data")
            val activities = data?.optJSONArray("activities")
            activities?.let { array ->
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val occurredAt = item.optString("occurredAt", "")
                    val date = if (occurredAt.isNotEmpty()) {
                        java.time.LocalDateTime.parse(occurredAt)
                            .atZone(ZoneId.systemDefault())
                            .toEpochSecond()
                    } else 0L

                    activityService.insertActivity(
                        date = date,
                        type = item.optString("type", ""),
                        protection = item.optString("protection", "") == "有保护",
                        pleasure = item.optInt("pleasureRating", 0),
                        mood = item.optString("healthStatus", ""),
                        notes = item.optString("notes", ""),
                        partnerId = null
                    )
                }
            }
        }
    }

    private suspend fun fetchCycles(userId: Long) {
        val response = apiService.get("/cycles?userId=$userId", authToken)
        response.getOrNull()?.let { jsonString ->
            val jsonObj = JSONObject(jsonString)
            val data = jsonObj.optJSONObject("data")
            val cycles = data?.optJSONArray("cycles")
            cycles?.let { array ->
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val startDateStr = item.optString("startDate", "")
                    val startDate = if (startDateStr.isNotEmpty()) {
                        java.time.LocalDate.parse(startDateStr)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toEpochSecond()
                    } else 0L

                    cycleService.insertCycle(
                        startDate = startDate,
                        duration = item.optInt("duration", 5),
                        predictedNext = item.optString("predictedNext", "").let { str ->
                            if (str.isNotEmpty()) {
                                java.time.LocalDate.parse(str)
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toEpochSecond()
                            } else null
                        }
                    )
                }
            }
        }
    }

    private suspend fun fetchMedications(userId: Long) {
        val response = apiService.get("/medications?userId=$userId", authToken)
        response.getOrNull()?.let { jsonString ->
            val jsonObj = JSONObject(jsonString)
            val data = jsonObj.optJSONObject("data")
            val medications = data?.optJSONArray("medications")
            medications?.let { array ->
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val reminderTime = item.optString("reminderTime", "").let { str ->
                        if (str.isNotEmpty()) {
                            java.time.LocalDateTime.parse(str)
                                .atZone(ZoneId.systemDefault())
                                .toEpochSecond()
                        } else null
                    }

                    medicationService.insertMedication(
                        name = item.optString("name", ""),
                        dosage = item.optString("dosage", ""),
                        reminderTime = reminderTime ?: 0L
                    )
                }
            }
        }
    }
}
