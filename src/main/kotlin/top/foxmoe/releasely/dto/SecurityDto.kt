package top.foxmoe.releasely.dto

import java.time.LocalDateTime

data class SecuritySettingsDto(
    val id: Long? = null,
    val userId: Long? = null,
    val lockType: String? = null,
    val isAppLockEnabled: Boolean = false,
    val isDisguiseEnabled: Boolean = false,
    val disguiseType: String? = null,
    val isScreenshotProtected: Boolean = true,
    val is2FAEnabled: Boolean = false,
    val lastUnlockTime: LocalDateTime? = null,
    val failedAttempts: Int = 0
)

data class UpdateSecuritySettingsRequest(
    val userId: Long,
    val lockType: String? = null,
    val isAppLockEnabled: Boolean? = null,
    val isDisguiseEnabled: Boolean? = null,
    val disguiseType: String? = null,
    val isScreenshotProtected: Boolean? = null
)

data class SetPinRequest(
    val userId: Long,
    val pin: String
)

data class VerifyPinRequest(
    val userId: Long,
    val pin: String
)

data class PinVerificationResponse(
    val success: Boolean,
    val attemptsRemaining: Int? = null,
    val lockedUntil: LocalDateTime? = null
)

data class Enable2FASetupResponse(
    val secret: String,
    val qrCodeUrl: String
)