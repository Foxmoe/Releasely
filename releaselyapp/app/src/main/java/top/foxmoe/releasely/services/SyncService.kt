package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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

    suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            syncActivities()
            syncCycles()
            syncMedications()
            syncPartners()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncActivities() {
        val activities = activityService.getAllActivities()
        val jsonArray = JSONArray()
        activities.forEach { activity ->
            val json = JSONObject().apply {
                put("id", activity.id)
                put("date", activity.date)
                put("type", activity.type)
                put("protection", activity.protection)
                put("pleasure", activity.pleasure)
                put("mood", activity.mood)
                put("notes", activity.notes)
                put("partnerId", activity.partnerId)
            }
            jsonArray.put(json)
        }

        val payload = JSONObject().apply {
            put("userId", 1L)
            put("activities", jsonArray)
        }

        apiService.post("/sync/activities", payload.toString(), authToken)
    }

    private suspend fun syncCycles() {
        val cycles = cycleService.getAllCycles()
        val jsonArray = JSONArray()
        cycles.forEach { cycle ->
            val json = JSONObject().apply {
                put("id", cycle.id)
                put("startDate", cycle.startDate)
                put("duration", cycle.duration)
                put("predictedNext", cycle.predictedNext)
            }
            jsonArray.put(json)
        }

        val payload = JSONObject().apply {
            put("userId", 1L)
            put("cycles", jsonArray)
        }

        apiService.post("/sync/cycles", payload.toString(), authToken)
    }

    private suspend fun syncMedications() {
        val medications = medicationService.getAllMedications()
        val jsonArray = JSONArray()
        medications.forEach { medication ->
            val json = JSONObject().apply {
                put("id", medication.id)
                put("name", medication.name)
                put("dosage", medication.dosage)
                put("reminderTime", medication.reminderTime)
                put("lastTaken", medication.lastTaken)
            }
            jsonArray.put(json)
        }

        val payload = JSONObject().apply {
            put("userId", 1L)
            put("medications", jsonArray)
        }

        apiService.post("/sync/medications", payload.toString(), authToken)
    }

    private suspend fun syncPartners() {
        val partners = partnerService.getAllPartners()
        val jsonArray = JSONArray()
        partners.forEach { partner ->
            val json = JSONObject().apply {
                put("id", partner.id)
                put("name", partner.name)
                put("inviteCode", partner.inviteCode)
                put("status", partner.status)
            }
            jsonArray.put(json)
        }

        val payload = JSONObject().apply {
            put("userId", 1L)
            put("partners", jsonArray)
        }

        apiService.post("/sync/partners", payload.toString(), authToken)
    }

    suspend fun fetchFromServer(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.get("/sync/pending", authToken)
            response.getOrNull()?.let { json ->
                val jsonObj = JSONObject(json)
                jsonObj.optJSONArray("activities")?.let { /* merge */ }
                jsonObj.optJSONArray("cycles")?.let { /* merge */ }
                jsonObj.optJSONArray("medications")?.let { /* merge */ }
                jsonObj.optJSONArray("partners")?.let { /* merge */ }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}