package top.foxmoe.releasely.shared.data.repository

import top.foxmoe.releasely.shared.data.remote.ApiService
import top.foxmoe.releasely.shared.data.remote.LoginRequestDto
import top.foxmoe.releasely.shared.domain.model.AuthToken
import top.foxmoe.releasely.shared.domain.model.LoginRequest
import top.foxmoe.releasely.shared.domain.model.LoginResult
import top.foxmoe.releasely.shared.domain.model.UserProfile
import top.foxmoe.releasely.shared.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResult> {
        return runCatching {
            val response = apiService.login(
                LoginRequestDto(
                    username = request.username,
                    password = request.password
                )
            )
            LoginResult(
                token = AuthToken(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken
                ),
                profile = UserProfile(
                    id = response.userId,
                    username = response.username,
                    displayName = response.displayName
                )
            )
        }
    }
}
