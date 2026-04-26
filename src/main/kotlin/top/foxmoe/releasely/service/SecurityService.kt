package top.foxmoe.releasely.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import top.foxmoe.releasely.entity.SecuritySettings
import top.foxmoe.releasely.mapper.SecuritySettingsMapper
import java.time.LocalDateTime

@Service
class SecurityService(
    private val securitySettingsMapper: SecuritySettingsMapper,
    private val passwordEncoder: PasswordEncoder
) {

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCK_DURATION_MINUTES = 30L
    }

    fun getSettingsByUserId(userId: Long): SecuritySettings? {
        return securitySettingsMapper.selectOne(QueryWrapper<SecuritySettings>()
            .eq("user_id", userId))
    }

    fun createDefaultSettings(userId: Long): SecuritySettings {
        val settings = SecuritySettings(
            userId = userId,
            lockType = SecuritySettings.LOCK_TYPE_NONE,
            isAppLockEnabled = false,
            isDisguiseEnabled = false,
            disguiseType = SecuritySettings.DISGUISE_CALCULATOR,
            isScreenshotProtected = true,
            is2FAEnabled = false,
            failedAttempts = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        securitySettingsMapper.insert(settings)
        return settings
    }

    fun getOrCreateSettings(userId: Long): SecuritySettings {
        return getSettingsByUserId(userId) ?: createDefaultSettings(userId)
    }

    fun updateSettings(settings: SecuritySettings): Boolean {
        settings.updatedAt = LocalDateTime.now()
        return securitySettingsMapper.updateById(settings) > 0
    }

    fun setPin(userId: Long, pin: String): Boolean {
        val settings = getOrCreateSettings(userId)
        settings.pinHash = passwordEncoder.encode(pin)
        settings.lockType = SecuritySettings.LOCK_TYPE_PIN
        settings.isAppLockEnabled = true
        return updateSettings(settings)
    }

    fun verifyPin(userId: Long, pin: String): PinVerificationResult {
        val settings = getSettingsByUserId(userId)
            ?: return PinVerificationResult(success = false, attemptsRemaining = MAX_FAILED_ATTEMPTS)

        if (settings.lockedUntil != null && settings.lockedUntil!!.isAfter(LocalDateTime.now())) {
            return PinVerificationResult(
                success = false,
                attemptsRemaining = 0,
                lockedUntil = settings.lockedUntil
            )
        }

        if (settings.pinHash == null || !passwordEncoder.matches(pin, settings.pinHash)) {
            settings.failedAttempts += 1
            if (settings.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                settings.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES)
            }
            updateSettings(settings)
            return PinVerificationResult(
                success = false,
                attemptsRemaining = MAX_FAILED_ATTEMPTS - settings.failedAttempts,
                lockedUntil = settings.lockedUntil
            )
        }

        settings.failedAttempts = 0
        settings.lockedUntil = null
        settings.lastUnlockTime = LocalDateTime.now()
        updateSettings(settings)

        return PinVerificationResult(success = true, attemptsRemaining = null, lockedUntil = null)
    }

    fun removePin(userId: Long): Boolean {
        val settings = getSettingsByUserId(userId) ?: return false
        settings.pinHash = null
        settings.lockType = SecuritySettings.LOCK_TYPE_NONE
        settings.isAppLockEnabled = false
        return updateSettings(settings)
    }

    fun enableDisguise(userId: Long, disguiseType: String): Boolean {
        val settings = getOrCreateSettings(userId)
        settings.isDisguiseEnabled = true
        settings.disguiseType = disguiseType
        return updateSettings(settings)
    }

    fun disableDisguise(userId: Long): Boolean {
        val settings = getSettingsByUserId(userId) ?: return false
        settings.isDisguiseEnabled = false
        return updateSettings(settings)
    }

    fun setScreenshotProtection(userId: Long, enabled: Boolean): Boolean {
        val settings = getOrCreateSettings(userId)
        settings.isScreenshotProtected = enabled
        return updateSettings(settings)
    }

    fun resetFailedAttempts(userId: Long): Boolean {
        val settings = getSettingsByUserId(userId) ?: return false
        settings.failedAttempts = 0
        settings.lockedUntil = null
        return updateSettings(settings)
    }

    data class PinVerificationResult(
        val success: Boolean,
        val attemptsRemaining: Int? = null,
        val lockedUntil: LocalDateTime? = null
    )
}