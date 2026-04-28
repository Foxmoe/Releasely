package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.Random

data class PartnerRecord(
    val id: String,
    val name: String,
    val inviteCode: String,
    val status: String,
    val sharedPermissions: String,
    val createdAt: Long
)

class PartnerService(private val queries: top.foxmoe.releasely.database.AppDatabaseQueries) {

    private val random = Random()

    suspend fun getAllPartners(): List<PartnerRecord> = withContext(Dispatchers.IO) {
        queries.getAllPartners().executeAsList().map { row ->
            PartnerRecord(
                id = row.id,
                name = row.name,
                inviteCode = row.inviteCode,
                status = row.status,
                sharedPermissions = row.sharedPermissions,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun getPartnerById(id: String): PartnerRecord? = withContext(Dispatchers.IO) {
        queries.getPartnerById(id).executeAsOneOrNull()?.let { row ->
            PartnerRecord(
                id = row.id,
                name = row.name,
                inviteCode = row.inviteCode,
                status = row.status,
                sharedPermissions = row.sharedPermissions,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun getPartnerByInviteCode(inviteCode: String): PartnerRecord? =
        withContext(Dispatchers.IO) {
            queries.getPartnerByInviteCode(inviteCode).executeAsOneOrNull()?.let { row ->
                PartnerRecord(
                    id = row.id,
                    name = row.name,
                    inviteCode = row.inviteCode,
                    status = row.status,
                    sharedPermissions = row.sharedPermissions,
                    createdAt = row.createdAt
                )
            }
        }

    suspend fun insertPartner(name: String): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val inviteCode = generateInviteCode()
        queries.insertPartner(
            id = id,
            name = name,
            inviteCode = inviteCode
        )
        id
    }

    suspend fun updatePartnerStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        queries.updatePartnerStatus(status, id)
    }

    suspend fun updatePartnerPermissions(id: String, permissions: String) =
        withContext(Dispatchers.IO) {
            queries.updatePartnerPermissions(permissions, id)
        }

    suspend fun deletePartner(id: String) = withContext(Dispatchers.IO) {
        queries.deletePartner(id)
    }

    suspend fun getPartnerCount(): Long = withContext(Dispatchers.IO) {
        queries.countPartners().executeAsOne()
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }
}