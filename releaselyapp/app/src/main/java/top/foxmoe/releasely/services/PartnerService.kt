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

class PartnerService(private val database: top.foxmoe.releasely.database.AppDatabase) {

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
}
