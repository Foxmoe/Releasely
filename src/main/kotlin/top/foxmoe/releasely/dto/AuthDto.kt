package top.foxmoe.releasely.dto

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null
)

data class AuthResponse(
    val token: String,
    val username: String,
    val userId: Long? = null,
    val requires2FA: Boolean = false
)

data class TwoFactorSetupResponse(
    val secret: String,
    val qrCodeUrl: String
)

data class TwoFactorLoginRequest(
    val preAuthToken: String,
    val totpCode: String
)

data class TwoFactorVerifyRequest(
    val userId: Long,
    val code: String
)

data class Enable2FARequest(
    val userId: Long,
    val code: String
)

data class Disable2FARequest(
    val userId: Long
)