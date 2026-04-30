package top.foxmoe.releasely.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import top.foxmoe.releasely.annotation.AuditLog
import top.foxmoe.releasely.dto.*
import top.foxmoe.releasely.entity.SecuritySettings
import top.foxmoe.releasely.service.SecurityService

@RestController
@RequestMapping("/api/security")
class SecurityController(private val securityService: SecurityService) {

    @GetMapping("/settings")
    fun getSettings(@RequestParam userId: Long): ResponseEntity<ApiResponse<SecuritySettingsDto>> {
        val settings = securityService.getOrCreateSettings(userId)
        return ResponseEntity.ok(ApiResponse.success(settings.toDto()))
    }

    @AuditLog(action = "SECURITY_SETTINGS_UPDATE", resourceType = "SECURITY")
    @PutMapping("/settings")
    fun updateSettings(@RequestBody request: UpdateSecuritySettingsRequest): ResponseEntity<ApiResponse<SecuritySettingsDto>> {
        val settings = securityService.getOrCreateSettings(request.userId)

        request.lockType?.let { settings.lockType = it }
        request.isAppLockEnabled?.let { settings.isAppLockEnabled = it }
        request.isDisguiseEnabled?.let { settings.isDisguiseEnabled = it }
        request.disguiseType?.let { settings.disguiseType = it }
        request.isScreenshotProtected?.let { settings.isScreenshotProtected = it }

        securityService.updateSettings(settings)
        return ResponseEntity.ok(ApiResponse.success(settings.toDto()))
    }

    @AuditLog(action = "PIN_SET", resourceType = "SECURITY")
    @PostMapping("/pin/set")
    fun setPin(@RequestBody request: SetPinRequest): ResponseEntity<ApiResponse<String>> {
        val success = securityService.setPin(request.userId, request.pin)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("PIN set successfully"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.INTERNAL_ERROR))
        }
    }

    @AuditLog(action = "PIN_VERIFY", resourceType = "SECURITY")
    @PostMapping("/pin/verify")
    fun verifyPin(@RequestBody request: VerifyPinRequest): ResponseEntity<ApiResponse<PinVerificationResponse>> {
        val result = securityService.verifyPin(request.userId, request.pin)
        return ResponseEntity.ok(ApiResponse.success(PinVerificationResponse(
            success = result.success,
            attemptsRemaining = result.attemptsRemaining,
            lockedUntil = result.lockedUntil
        )))
    }

    @AuditLog(action = "PIN_REMOVE", resourceType = "SECURITY")
    @DeleteMapping("/pin")
    fun removePin(@RequestParam userId: Long): ResponseEntity<ApiResponse<String>> {
        val success = securityService.removePin(userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("PIN removed"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @AuditLog(action = "DISGUISE_ENABLE", resourceType = "SECURITY")
    @PostMapping("/disguise/enable")
    fun enableDisguise(@RequestParam userId: Long, @RequestParam disguiseType: String): ResponseEntity<ApiResponse<String>> {
        val success = securityService.enableDisguise(userId, disguiseType)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Disguise enabled"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.INTERNAL_ERROR))
        }
    }

    @AuditLog(action = "DISGUISE_DISABLE", resourceType = "SECURITY")
    @PostMapping("/disguise/disable")
    fun disableDisguise(@RequestParam userId: Long): ResponseEntity<ApiResponse<String>> {
        val success = securityService.disableDisguise(userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Disguise disabled"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @AuditLog(action = "SCREENSHOT_PROTECTION_SET", resourceType = "SECURITY")
    @PostMapping("/screenshot")
    fun setScreenshotProtection(@RequestParam userId: Long, @RequestParam enabled: Boolean): ResponseEntity<ApiResponse<String>> {
        val success = securityService.setScreenshotProtection(userId, enabled)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("Screenshot protection updated"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.INTERNAL_ERROR))
        }
    }

    @AuditLog(action = "2FA_SETUP", resourceType = "SECURITY")
    @PostMapping("/2fa/setup")
    fun setup2FA(@RequestBody request: Setup2FARequest): ResponseEntity<ApiResponse<TwoFactorSetupResponse>> {
        val result = securityService.setup2FA(request.userId, request.username)
        return ResponseEntity.ok(ApiResponse.success(TwoFactorSetupResponse(
            secret = result.secret,
            qrCodeUrl = result.qrCodeUrl
        )))
    }

    @AuditLog(action = "2FA_ENABLE", resourceType = "SECURITY")
    @PostMapping("/2fa/enable")
    fun enable2FA(@RequestBody request: Enable2FARequest): ResponseEntity<ApiResponse<String>> {
        val success = securityService.enable2FA(request.userId, request.code)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("2FA enabled successfully"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.VALIDATE_FAILED))
        }
    }

    @AuditLog(action = "2FA_DISABLE", resourceType = "SECURITY")
    @PostMapping("/2fa/disable")
    fun disable2FA(@RequestBody request: Disable2FARequest): ResponseEntity<ApiResponse<String>> {
        val success = securityService.disable2FA(request.userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse.success("2FA disabled successfully"))
        } else {
            ResponseEntity.ok(ApiResponse.error(ResultCode.NOT_FOUND))
        }
    }

    @AuditLog(action = "2FA_VERIFY", resourceType = "SECURITY")
    @PostMapping("/2fa/verify")
    fun verify2FA(@RequestBody request: TwoFactorVerifyRequest): ResponseEntity<ApiResponse<Boolean>> {
        val valid = securityService.verify2FA(request.userId, request.code)
        return ResponseEntity.ok(ApiResponse.success(valid))
    }

    private fun SecuritySettings.toDto() = SecuritySettingsDto(
        id = id,
        userId = userId,
        lockType = lockType,
        isAppLockEnabled = isAppLockEnabled,
        isDisguiseEnabled = isDisguiseEnabled,
        disguiseType = disguiseType,
        isScreenshotProtected = isScreenshotProtected,
        is2FAEnabled = is2FAEnabled,
        lastUnlockTime = lastUnlockTime,
        failedAttempts = failedAttempts
    )
}
