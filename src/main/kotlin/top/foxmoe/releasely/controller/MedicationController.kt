package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.Medication
import top.foxmoe.releasely.service.MedicationService

@RestController
@RequestMapping("/api/medications")
class MedicationController(private val medicationService: MedicationService) {

    @GetMapping
    fun getMedications(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Medication>>> {
        val medications = medicationService.getMedicationsByUserId(userId)
        return ResponseEntity.ok(ApiResponse.success(medications))
    }

    @GetMapping("/active")
    fun getActiveMedications(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Medication>>> {
        val medications = medicationService.getActiveMedications(userId)
        return ResponseEntity.ok(ApiResponse.success(medications))
    }

    @GetMapping("/reminders")
    fun getMedicationsNeedingReminder(@RequestParam userId: Long): ResponseEntity<ApiResponse<List<Medication>>> {
        val medications = medicationService.getMedicationsNeedingReminder(userId)
        return ResponseEntity.ok(ApiResponse.success(medications))
    }

    @GetMapping("/{id}")
    fun getMedicationById(@PathVariable id: Long): ResponseEntity<ApiResponse<Medication>> {
        val medication = medicationService.getMedicationById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        return ResponseEntity.ok(ApiResponse.success(medication))
    }

    @AuditLog(action = "MEDICATION_CREATE", resourceType = "MEDICATION")
    @PostMapping
    fun createMedication(@RequestBody request: CreateMedicationRequest): ResponseEntity<ApiResponse<Medication>> {
        val medication = Medication(
            userId = request.userId,
            name = request.name,
            dosage = request.dosage,
            reminderTime = request.reminderTime
        )
        val id = medicationService.createMedication(medication)
        val createdMedication = medicationService.getMedicationById(id)
        return ResponseEntity.ok(ApiResponse.success(createdMedication))
    }

    @AuditLog(action = "MEDICATION_UPDATE", resourceType = "MEDICATION")
    @PutMapping("/{id}")
    fun updateMedication(
        @PathVariable id: Long,
        @RequestBody request: UpdateMedicationRequest
    ): ResponseEntity<ApiResponse<Medication>> {
        val existingMedication = medicationService.getMedicationById(id)
            ?: return ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))

        request.name?.let { existingMedication.name = it }
        request.dosage?.let { existingMedication.dosage = it }
        request.reminderTime?.let { existingMedication.reminderTime = it }
        request.isActive?.let { existingMedication.isActive = it }

        medicationService.updateMedication(existingMedication)
        return ResponseEntity.ok(ApiResponse.success(existingMedication))
    }

    @AuditLog(action = "MEDICATION_DELETE", resourceType = "MEDICATION")
    @DeleteMapping("/{id}")
    fun deleteMedication(@PathVariable id: Long): ResponseEntity<ApiResponse<Nothing>> {
        val success = medicationService.deleteMedication(id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Medication deleted"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @AuditLog(action = "MEDICATION_TAKEN", resourceType = "MEDICATION")
    @PostMapping("/taken")
    fun recordTaken(@RequestBody request: MedicationTakenRequest): ResponseEntity<ApiResponse<Nothing>> {
        val success = medicationService.recordTaken(request.id)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Medication recorded as taken"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }
}
