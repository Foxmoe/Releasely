package top.foxmoe.releasely.shared.domain.usecase

import top.foxmoe.releasely.shared.domain.model.LoginRequest
import top.foxmoe.releasely.shared.domain.model.LoginResult
import top.foxmoe.releasely.shared.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<LoginResult> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("username/password can not be empty"))
        }
        return authRepository.login(LoginRequest(username.trim(), password))
    }
}
