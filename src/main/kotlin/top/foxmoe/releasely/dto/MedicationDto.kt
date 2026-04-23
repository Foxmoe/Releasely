package top.foxmoe.releasely.dto

import java.time.LocalDateTime

data class MedicationDto(
    val id: Long? = null,
    val userId: Long? = null,
    val name: String? = null,
    val dosage: String? = null,
    val reminderTime: LocalDateTime? = null,
    val lastTaken: LocalDateTime? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime? = null,
    val needsReminder: Boolean = false
)

data class CreateMedicationRequest(
    val userId: Long,
    val name: String,
    val dosage: String,
    val reminderTime: LocalDateTime
)

data class UpdateMedicationRequest(
    val id: Long,
    val name: String? = null,
    val dosage: String? = null,
    val reminderTime: LocalDateTime? = null,
    val isActive: Boolean? = null
)

data class MedicationTakenRequest(
    val id: Long
)