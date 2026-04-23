package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.stereotype.Service
import top.foxmoe.releasely.entity.Medication
import top.foxmoe.releasely.mapper.MedicationMapper
import java.time.LocalDateTime

@Service
class MedicationService(private val medicationMapper: MedicationMapper) {

    fun getMedicationsByUserId(userId: Long): List<Medication> {
        val wrapper = QueryWrapper<Medication>()
            .eq("user_id", userId)
            .eq("is_active", true)
            .orderByDesc("created_at")
        return medicationMapper.selectList(wrapper)
    }

    fun getMedicationById(id: Long): Medication? {
        return medicationMapper.selectById(id)
    }

    fun createMedication(medication: Medication): Long {
        medication.createdAt = LocalDateTime.now()
        medication.isActive = true
        medicationMapper.insert(medication)
        return medication.id ?: 0L
    }

    fun updateMedication(medication: Medication): Boolean {
        return medicationMapper.updateById(medication) > 0
    }

    fun deleteMedication(id: Long): Boolean {
        val medication = medicationMapper.selectById(id)
        if (medication != null) {
            medication.isActive = false
            return medicationMapper.updateById(medication) > 0
        }
        return false
    }

    fun recordTaken(id: Long): Boolean {
        val medication = medicationMapper.selectById(id)
        if (medication != null) {
            medication.lastTaken = LocalDateTime.now()
            return medicationMapper.updateById(medication) > 0
        }
        return false
    }

    fun getActiveMedications(userId: Long): List<Medication> {
        val wrapper = QueryWrapper<Medication>()
            .eq("user_id", userId)
            .eq("is_active", true)
            .orderByAsc("reminder_time")
        return medicationMapper.selectList(wrapper)
    }

    fun getMedicationsNeedingReminder(userId: Long): List<Medication> {
        val medications = getActiveMedications(userId)
        val now = LocalDateTime.now()
        return medications.filter { medication ->
            val reminderTime = medication.reminderTime
            reminderTime != null && reminderTime.isBefore(now.plusHours(1))
        }
    }
}