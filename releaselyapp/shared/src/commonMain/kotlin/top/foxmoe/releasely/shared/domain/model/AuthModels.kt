package top.foxmoe.releasely.shared.domain.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthToken(
    val accessToken: String,
    val refreshToken: String
)

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String
)

data class LoginResult(
    val token: AuthToken,
    val profile: UserProfile
)
