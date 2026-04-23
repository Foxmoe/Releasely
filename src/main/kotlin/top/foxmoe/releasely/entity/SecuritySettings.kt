package top.foxmoe.releasely.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("security_settings")
data class SecuritySettings(
    @TableId(type = IdType.AUTO)
    var id: Long? = null,
    var userId: Long? = null,
    var lockType: String? = null, // PIN, BIOMETRIC, NONE
    var pinHash: String? = null,
    var isAppLockEnabled: Boolean = false,
    var isDisguiseEnabled: Boolean = false,
    var disguiseType: String? = null, // CALCULATOR, WEATHER, NEWS
    var isScreenshotProtected: Boolean = true,
    var is2FAEnabled: Boolean = false,
    var twoFactorSecret: String? = null,
    var lastUnlockTime: LocalDateTime? = null,
    var failedAttempts: Int = 0,
    var lockedUntil: LocalDateTime? = null,
    var createdAt: LocalDateTime? = null,
    var updatedAt: LocalDateTime? = null
) {
    companion object {
        const val LOCK_TYPE_NONE = "NONE"
        const val LOCK_TYPE_PIN = "PIN"
        const val LOCK_TYPE_BIOMETRIC = "BIOMETRIC"

        const val DISGUISE_CALCULATOR = "CALCULATOR"
        const val DISGUISE_WEATHER = "WEATHER"
        const val DISGUISE_NEWS = "NEWS"
    }
}