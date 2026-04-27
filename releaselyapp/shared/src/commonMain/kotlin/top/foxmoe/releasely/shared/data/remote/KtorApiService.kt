package top.foxmoe.releasely.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : ApiService {

    override suspend fun login(request: LoginRequestDto): LoginResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
