package top.foxmoe.releasely.shared.domain.repository

import top.foxmoe.releasely.shared.domain.model.LoginRequest
import top.foxmoe.releasely.shared.domain.model.LoginResult

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResult>
}
