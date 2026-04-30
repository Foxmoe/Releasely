package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.Random

data class PartnerRecord(
    val id: String,
    val serverId: String?,
    val name: String,
    val inviteCode: String,
    val status: String,
    val sharedPermissions: String?,
    val isDeleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class CreateInviteCodeResponse(
    val inviteCode: String,
    val inviteLink: String
)

data class InvitePartnerRequest(
    val userId: Long,
    val partnerInviteCode: String,
    val sharedPermissions: String = "calendar,records"
)

data class AcceptInviteRequest(
    val inviteCode: String,
    val userId: Long
)

class PartnerService(
    private val database: top.foxmoe.releasely.database.AppDatabase,
    private val apiService: ApiService
) {

    private val random = Random()

    suspend fun getAllPartners(): List<PartnerRecord> = withContext(Dispatchers.IO) {
        database.partnerQueries.getAllPartners().executeAsList().map { row ->
            PartnerRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                inviteCode = row.invite_code,
                status = row.status,
                sharedPermissions = row.shared_permissions,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getPartnerById(id: String): PartnerRecord? = withContext(Dispatchers.IO) {
        database.partnerQueries.getPartnerById(id).executeAsOneOrNull()?.let { row ->
            PartnerRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                inviteCode = row.invite_code,
                status = row.status,
                sharedPermissions = row.shared_permissions,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getPartnerByServerId(serverId: String): PartnerRecord? = withContext(Dispatchers.IO) {
        database.partnerQueries.getPartnerByServerId(serverId).executeAsOneOrNull()?.let { row ->
            PartnerRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                inviteCode = row.invite_code,
                status = row.status,
                sharedPermissions = row.shared_permissions,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getPartnerByInviteCode(inviteCode: String): PartnerRecord? =
        withContext(Dispatchers.IO) {
            database.partnerQueries.getPartnerByInviteCode(inviteCode).executeAsOneOrNull()?.let { row ->
                PartnerRecord(
                    id = row.id,
                    serverId = row.server_id,
                    name = row.name,
                    inviteCode = row.invite_code,
                    status = row.status,
                    sharedPermissions = row.shared_permissions,
                    isDeleted = row.is_deleted == 1L,
                    createdAt = row.created_at,
                    updatedAt = row.updated_at
                )
            }
        }

    suspend fun insertPartner(name: String): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val invite_code = generateInviteCode()
        database.partnerQueries.insertPartner(
            id = id,
            server_id = null,
            name = name,
            invite_code = invite_code
        )
        id
    }

    suspend fun updatePartnerStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        database.partnerQueries.updatePartnerStatus(status, id)
    }

    suspend fun updatePartnerPermissions(id: String, permissions: String) =
        withContext(Dispatchers.IO) {
            database.partnerQueries.updatePartnerPermissions(permissions, id)
        }

    suspend fun deletePartner(id: String) = withContext(Dispatchers.IO) {
        database.partnerQueries.markPartnerDeleted(id)
    }

    suspend fun getPartnersUpdatedSince(since: Long): List<PartnerRecord> = withContext(Dispatchers.IO) {
        database.partnerQueries.getPartnersUpdatedSince(since).executeAsList().map { row ->
            PartnerRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                inviteCode = row.invite_code,
                status = row.status,
                sharedPermissions = row.shared_permissions,
                isDeleted = row.is_deleted == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun setServerId(localId: String, serverId: String) = withContext(Dispatchers.IO) {
        database.partnerQueries.setServerId(serverId, localId)
    }

    suspend fun getPartnerCount(): Long = withContext(Dispatchers.IO) {
        database.partnerQueries.countPartners().executeAsOne()
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    /**
     * Create an invite code on the server and return the invite code and link
     */
    suspend fun invitePartner(userId: Long): Result<CreateInviteCodeResponse> = withContext(Dispatchers.IO) {
        try {
            val request = InvitePartnerRequest(userId = userId, partnerInviteCode = "")
            val response = apiService.post("/api/partners/invite", json = request.toJson())
            response.fold(
                onSuccess = { json ->
                    val data = parseApiResponse<CreateInviteCodeResponse>(json)
                    if (data != null) {
                        Result.success(data)
                    } else {
                        Result.failure(Exception("Failed to parse response"))
                    }
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Accept an invite using the invite code
     */
    suspend fun acceptInvite(inviteCode: String, userId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = AcceptInviteRequest(inviteCode = inviteCode, userId = userId)
            val response = apiService.post("/api/partners/accept", json = request.toJson())
            response.fold(
                onSuccess = { json ->
                    val data = parseApiResponse<Any>(json)
                    Result.success(data != null)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun InvitePartnerRequest.toJson(): String {
        return """{"userId":$userId,"partnerInviteCode":"$partnerInviteCode","sharedPermissions":"$sharedPermissions"}"""
    }

    private fun AcceptInviteRequest.toJson(): String {
        return """{"inviteCode":"$inviteCode","userId":$userId}"""
    }

    private inline fun <reified T> parseApiResponse(json: String): T? {
        return try {
            // Simple JSON parsing - extract data field
            val dataMatch = Regex(""""data"\s*:\s*(\{[^}]*\})""").find(json)
            dataMatch?.groupValues?.get(1)?.let { dataStr ->
                when (T::class) {
                    CreateInviteCodeResponse::class -> {
                        val codeMatch = Regex(""""inviteCode"\s*:\s*"([^"]*)"""").find(dataStr)
                        val linkMatch = Regex(""""inviteLink"\s*:\s*"([^"]*)"""").find(dataStr)
                        if (codeMatch != null && linkMatch != null) {
                            CreateInviteCodeResponse(
                                inviteCode = codeMatch.groupValues[1],
                                inviteLink = linkMatch.groupValues[1]
                            ) as T
                        } else null
                    }
                    else -> null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
