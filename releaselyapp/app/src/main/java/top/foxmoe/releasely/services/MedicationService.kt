package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class MedicationRecord(
    val id: String,
    val serverId: String?,
    val name: String,
    val dosage: String,
    val reminderTime: Long,
    val lastTaken: Long?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

class MedicationService(private val database: top.foxmoe.releasely.database.AppDatabase) {

    suspend fun getAllMedications(): List<MedicationRecord> = withContext(Dispatchers.IO) {
        database.medicationQueries.getAllMedications().executeAsList().map { row ->
            MedicationRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                dosage = row.dosage,
                reminderTime = row.reminder_time,
                lastTaken = row.last_taken,
                isActive = row.is_active == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getMedicationById(id: String): MedicationRecord? = withContext(Dispatchers.IO) {
        database.medicationQueries.getMedicationById(id).executeAsOneOrNull()?.let { row ->
            MedicationRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                dosage = row.dosage,
                reminderTime = row.reminder_time,
                lastTaken = row.last_taken,
                isActive = row.is_active == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getMedicationByServerId(serverId: String): MedicationRecord? = withContext(Dispatchers.IO) {
        database.medicationQueries.getMedicationByServerId(serverId).executeAsOneOrNull()?.let { row ->
            MedicationRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                dosage = row.dosage,
                reminderTime = row.reminder_time,
                lastTaken = row.last_taken,
                isActive = row.is_active == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun insertMedication(
        name: String,
        dosage: String,
        reminderTime: Long
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        database.medicationQueries.insertMedication(
            id = id,
            server_id = null,
            name = name,
            dosage = dosage,
            reminder_time = reminderTime
        )
        id
    }

    suspend fun updateMedication(
        id: String,
        name: String,
        dosage: String,
        reminderTime: Long,
        lastTaken: Long?
    ) = withContext(Dispatchers.IO) {
        database.medicationQueries.updateMedication(
            name = name,
            dosage = dosage,
            reminder_time = reminderTime,
            last_taken = lastTaken,
            id = id
        )
    }

    suspend fun deleteMedication(id: String) = withContext(Dispatchers.IO) {
        database.medicationQueries.markMedicationInactive(id)
    }

    suspend fun markTaken(id: String) = withContext(Dispatchers.IO) {
        database.medicationQueries.markTaken(id)
    }

    suspend fun getActiveMedications(): List<MedicationRecord> = withContext(Dispatchers.IO) {
        database.medicationQueries.getActiveMedications().executeAsList().map { row ->
            MedicationRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                dosage = row.dosage,
                reminderTime = row.reminder_time,
                lastTaken = row.last_taken,
                isActive = row.is_active == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun getMedicationsUpdatedSince(since: Long): List<MedicationRecord> = withContext(Dispatchers.IO) {
        database.medicationQueries.getMedicationsUpdatedSince(since).executeAsList().map { row ->
            MedicationRecord(
                id = row.id,
                serverId = row.server_id,
                name = row.name,
                dosage = row.dosage,
                reminderTime = row.reminder_time,
                lastTaken = row.last_taken,
                isActive = row.is_active == 1L,
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    suspend fun setServerId(localId: String, serverId: String) = withContext(Dispatchers.IO) {
        database.medicationQueries.setServerId(serverId, localId)
    }

    suspend fun getMedicationCount(): Long = withContext(Dispatchers.IO) {
        database.medicationQueries.countMedications().executeAsOne()
    }
}
