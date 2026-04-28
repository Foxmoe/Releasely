package top.foxmoe.releasely.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class MedicationRecord(
    val id: String,
    val name: String,
    val dosage: String,
    val reminderTime: Long,
    val lastTaken: Long?,
    val isActive: Boolean,
    val createdAt: Long
)

class MedicationService(private val queries: top.foxmoe.releasely.database.AppDatabaseQueries) {

    suspend fun getAllMedications(): List<MedicationRecord> = withContext(Dispatchers.IO) {
        queries.getAllMedications().executeAsList().map { row ->
            MedicationRecord(
                id = row.id,
                name = row.name,
                dosage = row.dosage,
                reminderTime = row.reminderTime,
                lastTaken = row.lastTaken,
                isActive = row.isActive == 1L,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun getMedicationById(id: String): MedicationRecord? = withContext(Dispatchers.IO) {
        queries.getMedicationById(id).executeAsOneOrNull()?.let { row ->
            MedicationRecord(
                id = row.id,
                name = row.name,
                dosage = row.dosage,
                reminderTime = row.reminderTime,
                lastTaken = row.lastTaken,
                isActive = row.isActive == 1L,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun insertMedication(
        name: String,
        dosage: String,
        reminderTime: Long
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        queries.insertMedication(
            id = id,
            name = name,
            dosage = dosage,
            reminderTime = reminderTime
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
        queries.updateMedication(
            name = name,
            dosage = dosage,
            reminderTime = reminderTime,
            lastTaken = lastTaken,
            id = id
        )
    }

    suspend fun deleteMedication(id: String) = withContext(Dispatchers.IO) {
        queries.deleteMedication(id)
    }

    suspend fun markTaken(id: String) = withContext(Dispatchers.IO) {
        queries.markTaken(id)
    }

    suspend fun getActiveMedications(): List<MedicationRecord> = withContext(Dispatchers.IO) {
        queries.getActiveMedications().executeAsList().map { row ->
            MedicationRecord(
                id = row.id,
                name = row.name,
                dosage = row.dosage,
                reminderTime = row.reminderTime,
                lastTaken = row.lastTaken,
                isActive = row.isActive == 1L,
                createdAt = row.createdAt
            )
        }
    }

    suspend fun getMedicationCount(): Long = withContext(Dispatchers.IO) {
        queries.countMedications().executeAsOne()
    }
}