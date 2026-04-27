package top.foxmoe.releasely.shared.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ApiService {
    suspend fun login(request: LoginRequestDto): LoginResponseDto
}

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("user_id")
    val userId: String,
    val username: String,
    @SerialName("display_name")
    val displayName: String
)
