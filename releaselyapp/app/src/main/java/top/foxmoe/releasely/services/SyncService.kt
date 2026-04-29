package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId

class SyncService(
    private val apiService: ApiService,
    private val syncMetaService: SyncMetaService,
    private val activityService: ActivityService,
    private val cycleService: CycleService,
    private val medicationService: MedicationService,
    private val partnerService: PartnerService
) {
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    /**
     * 全量同步：先推送本地变更，再拉取服务端变更
     */
    suspend fun syncAll(userId: Long): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val now = System.currentTimeMillis() / 1000
            val pushResult = pushAll(userId)
            val pullResult = pullAll(userId)
            Result.success(SyncResult(pushed = pushResult, pulled = pullResult, timestamp = now))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 增量推送：只推送 updated_at > last_synced_at 的记录
     */
    private suspend fun pushAll(userId: Long): Int {
        var count = 0
        count += pushActivities(userId)
        count += pushCycles(userId)
        count += pushMedications(userId)
        count += pushPartners(userId)
        return count
    }

    private suspend fun pushActivities(userId: Long): Int {
        val lastSync = syncMetaService.getLastSync("activity")
        val activities = activityService.getActivitiesUpdatedSince(lastSync)
        if (activities.isEmpty()) return 0

        var pushed = 0
        activities.forEach { activity ->
            val json = JSONObject().apply {
                put("userId", userId)
                put("type", activity.type)
                put("protection", if (activity.protection) "有保护" else "无保护")
                put("pleasureRating", activity.pleasure ?: 0)
                put("healthStatus", activity.mood ?: "")
                put("occurredAt", epochSecondToLocalDateTime(activity.date))
                put("notes", activity.notes ?: "")
                put("isDeleted", activity.isDeleted)
            }
            val result = apiService.post("/activity", json.toString(), authToken)
            result.getOrNull()?.let { response ->
                val serverId = extractIdFromResponse(response)
                if (serverId != null && activity.serverId == null) {
                    activityService.setServerId(activity.id, serverId.toString())
                }
                pushed++
            }
        }
        return pushed
    }

    private suspend fun pushCycles(userId: Long): Int {
        val lastSync = syncMetaService.getLastSync("cycle")
        val cycles = cycleService.getCyclesUpdatedSince(lastSync)
        if (cycles.isEmpty()) return 0

        var pushed = 0
        cycles.forEach { cycle ->
            val json = JSONObject().apply {
                put("userId", userId)
                put("startDate", epochSecondToLocalDate(cycle.startDate))
                put("duration", cycle.duration ?: 5)
                put("predictedNext", cycle.predictedNext?.let { epochSecondToLocalDate(it) })
                put("isDeleted", cycle.isDeleted)
            }
            val result = apiService.post("/cycles", json.toString(), authToken)
            result.getOrNull()?.let { response ->
                val serverId = extractIdFromResponse(response)
                if (serverId != null && cycle.serverId == null) {
                    cycleService.setServerId(cycle.id, serverId.toString())
                }
                pushed++
            }
        }
        return pushed
    }

    private suspend fun pushMedications(userId: Long): Int {
        val lastSync = syncMetaService.getLastSync("medication")
        val medications = medicationService.getMedicationsUpdatedSince(lastSync)
        if (medications.isEmpty()) return 0

        var pushed = 0
        medications.forEach { med ->
            val json = JSONObject().apply {
                put("userId", userId)
                put("name", med.name)
                put("dosage", med.dosage)
                put("reminderTime", epochSecondToLocalDateTime(med.reminderTime))
                put("lastTaken", med.lastTaken?.let { epochSecondToLocalDateTime(it) })
                put("isActive", med.isActive)
            }
            val result = apiService.post("/medications", json.toString(), authToken)
            result.getOrNull()?.let { response ->
                val serverId = extractIdFromResponse(response)
                if (serverId != null && med.serverId == null) {
                    medicationService.setServerId(med.id, serverId.toString())
                }
                pushed++
            }
        }
        return pushed
    }

    private suspend fun pushPartners(userId: Long): Int {
        val lastSync = syncMetaService.getLastSync("partner")
        val partners = partnerService.getPartnersUpdatedSince(lastSync)
        if (partners.isEmpty()) return 0

        var pushed = 0
        partners.forEach { partner ->
            val json = JSONObject().apply {
                put("userId", userId)
                put("name", partner.name)
                put("status", partner.status)
                put("sharedPermissions", partner.sharedPermissions ?: "{}")
                put("isDeleted", partner.isDeleted)
            }
            val result = apiService.post("/partners", json.toString(), authToken)
            result.getOrNull()?.let { response ->
                val serverId = extractIdFromResponse(response)
                if (serverId != null && partner.serverId == null) {
                    partnerService.setServerId(partner.id, serverId.toString())
                }
                pushed++
            }
        }
        return pushed
    }

    /**
     * 增量拉取：拉取服务端数据并与本地合并
     */
    private suspend fun pullAll(userId: Long): Int {
        var count = 0
        count += pullActivities(userId)
        count += pullCycles(userId)
        count += pullMedications(userId)
        count += pullPartners(userId)

        val now = System.currentTimeMillis() / 1000
        syncMetaService.setLastSync("activity", now)
        syncMetaService.setLastSync("cycle", now)
        syncMetaService.setLastSync("medication", now)
        syncMetaService.setLastSync("partner", now)
        return count
    }

    private suspend fun pullActivities(userId: Long): Int {
        val response = apiService.get("/activity/list?userId=$userId", authToken)
        return response.getOrNull()?.let { jsonString ->
            val array = extractArrayFromResponse(jsonString, "activities")
            var count = 0
            array?.let {
                for (i in 0 until it.length()) {
                    val item = it.getJSONObject(i)
                    val serverId = item.optLong("id", -1).toString()
                    if (serverId == "-1") continue

                    val occurredAt = item.optString("occurredAt", "")
                    val date = parseDateTimeToEpoch(occurredAt)

                    val existing = activityService.getActivityByServerId(serverId)
                    if (existing == null) {
                        activityService.insertActivity(
                            date = date,
                            type = item.optString("type", ""),
                            protection = item.optString("protection", "") == "有保护",
                            pleasure = item.optInt("pleasureRating", 0).takeIf { it > 0 },
                            mood = item.optString("healthStatus", "").takeIf { it.isNotEmpty() },
                            notes = item.optString("notes", "").takeIf { it.isNotEmpty() },
                            partnerId = null
                        )
                        // 新插入的记录需要设置 server_id
                        val latest = activityService.getAllActivities().firstOrNull { it.serverId == null && it.date == date }
                        latest?.let { activityService.setServerId(it.id, serverId) }
                    } else {
                        // 服务端数据较新则覆盖
                        val serverUpdatedAt = item.optString("updatedAt", "")
                        if (shouldOverwrite(serverUpdatedAt, existing.updatedAt)) {
                            activityService.updateActivity(
                                id = existing.id,
                                date = date,
                                type = item.optString("type", existing.type),
                                protection = item.optString("protection", "") == "有保护",
                                pleasure = item.optInt("pleasureRating", 0).takeIf { it > 0 } ?: existing.pleasure,
                                mood = item.optString("healthStatus", "").takeIf { it.isNotEmpty() } ?: existing.mood,
                                notes = item.optString("notes", "").takeIf { it.isNotEmpty() } ?: existing.notes,
                                partnerId = existing.partnerId
                            )
                        }
                    }
                    count++
                }
            }
            count
        } ?: 0
    }

    private suspend fun pullCycles(userId: Long): Int {
        val response = apiService.get("/cycles?userId=$userId", authToken)
        return response.getOrNull()?.let { jsonString ->
            val array = extractArrayFromResponse(jsonString)
            var count = 0
            array?.let {
                for (i in 0 until it.length()) {
                    val item = it.getJSONObject(i)
                    val serverId = item.optLong("id", -1).toString()
                    if (serverId == "-1") continue

                    val startDate = parseDateToEpoch(item.optString("startDate", ""))
                    val predictedNext = item.optString("predictedNext", "").let { str ->
                        if (str.isNotEmpty()) parseDateToEpoch(str) else null
                    }

                    val existing = cycleService.getCycleByServerId(serverId)
                    if (existing == null) {
                        cycleService.insertCycle(
                            startDate = startDate,
                            duration = item.optInt("duration", 5),
                            predictedNext = predictedNext
                        )
                        val latest = cycleService.getAllCycles().firstOrNull { it.serverId == null && it.startDate == startDate }
                        latest?.let { cycleService.setServerId(it.id, serverId) }
                    } else {
                        val serverUpdatedAt = item.optString("updatedAt", "")
                        if (shouldOverwrite(serverUpdatedAt, existing.updatedAt)) {
                            cycleService.updateCycle(
                                id = existing.id,
                                startDate = startDate,
                                duration = item.optInt("duration", existing.duration ?: 5),
                                predictedNext = predictedNext ?: existing.predictedNext
                            )
                        }
                    }
                    count++
                }
            }
            count
        } ?: 0
    }

    private suspend fun pullMedications(userId: Long): Int {
        val response = apiService.get("/medications?userId=$userId", authToken)
        return response.getOrNull()?.let { jsonString ->
            val array = extractArrayFromResponse(jsonString)
            var count = 0
            array?.let {
                for (i in 0 until it.length()) {
                    val item = it.getJSONObject(i)
                    val serverId = item.optLong("id", -1).toString()
                    if (serverId == "-1") continue

                    val reminderTime = parseDateTimeToEpoch(item.optString("reminderTime", ""))
                    val lastTaken = item.optString("lastTaken", "").let { str ->
                        if (str.isNotEmpty()) parseDateTimeToEpoch(str) else null
                    }

                    val existing = medicationService.getMedicationByServerId(serverId)
                    if (existing == null) {
                        medicationService.insertMedication(
                            name = item.optString("name", ""),
                            dosage = item.optString("dosage", ""),
                            reminderTime = reminderTime
                        )
                        val latest = medicationService.getAllMedications().firstOrNull { it.serverId == null && it.name == item.optString("name", "") }
                        latest?.let { medicationService.setServerId(it.id, serverId) }
                    } else {
                        val serverUpdatedAt = item.optString("updatedAt", "")
                        if (shouldOverwrite(serverUpdatedAt, existing.updatedAt)) {
                            medicationService.updateMedication(
                                id = existing.id,
                                name = item.optString("name", existing.name),
                                dosage = item.optString("dosage", existing.dosage),
                                reminderTime = reminderTime,
                                lastTaken = lastTaken ?: existing.lastTaken
                            )
                        }
                    }
                    count++
                }
            }
            count
        } ?: 0
    }

    private suspend fun pullPartners(userId: Long): Int {
        val response = apiService.get("/partners?userId=$userId", authToken)
        return response.getOrNull()?.let { jsonString ->
            val array = extractArrayFromResponse(jsonString)
            var count = 0
            array?.let {
                for (i in 0 until it.length()) {
                    val item = it.getJSONObject(i)
                    val serverId = item.optLong("id", -1).toString()
                    if (serverId == "-1") continue

                    val existing = partnerService.getPartnerByServerId(serverId)
                    if (existing == null) {
                        // Partner 由服务端创建，本地不主动创建新的 partner
                        // 仅更新已有 partner 的状态
                    } else {
                        val serverUpdatedAt = item.optString("updatedAt", "")
                        if (shouldOverwrite(serverUpdatedAt, existing.updatedAt)) {
                            partnerService.updatePartnerStatus(existing.id, item.optString("status", existing.status))
                            partnerService.updatePartnerPermissions(
                                existing.id,
                                item.optString("sharedPermissions", existing.sharedPermissions ?: "{}")
                            )
                        }
                    }
                    count++
                }
            }
            count
        } ?: 0
    }

    // ---- 工具方法 ----

    private fun extractIdFromResponse(response: String): Long? {
        return try {
            val json = JSONObject(response)
            val data = json.optJSONObject("data")
            data?.optLong("id", -1)?.takeIf { it > 0 }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractArrayFromResponse(jsonString: String, fieldName: String? = null): JSONArray? {
        return try {
            val json = JSONObject(jsonString)
            if (fieldName != null) {
                json.optJSONObject("data")?.optJSONArray(fieldName)
            } else {
                val data = json.get("data")
                if (data is JSONArray) data else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun epochSecondToLocalDateTime(epoch: Long): String {
        return Instant.ofEpochSecond(epoch)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime().toString()
    }

    private fun epochSecondToLocalDate(epoch: Long): String {
        return Instant.ofEpochSecond(epoch)
            .atZone(ZoneId.systemDefault())
            .toLocalDate().toString()
    }

    private fun parseDateTimeToEpoch(dateStr: String): Long {
        return if (dateStr.isNotEmpty()) {
            try {
                java.time.LocalDateTime.parse(dateStr)
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond()
            } catch (e: Exception) {
                0L
            }
        } else 0L
    }

    private fun parseDateToEpoch(dateStr: String): Long {
        return if (dateStr.isNotEmpty()) {
            try {
                java.time.LocalDate.parse(dateStr)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toEpochSecond()
            } catch (e: Exception) {
                0L
            }
        } else 0L
    }

    private fun shouldOverwrite(serverUpdatedAtStr: String, localUpdatedAt: Long): Boolean {
        if (serverUpdatedAtStr.isEmpty()) return false
        return try {
            val serverTime = java.time.LocalDateTime.parse(serverUpdatedAtStr)
                .atZone(ZoneId.systemDefault())
                .toEpochSecond()
            serverTime > localUpdatedAt
        } catch (e: Exception) {
            false
        }
    }
}

data class SyncResult(
    val pushed: Int,
    val pulled: Int,
    val timestamp: Long
)
