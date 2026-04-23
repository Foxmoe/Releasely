package top.foxmoe.releasely.dto

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse(code = 200, message = "success", data = data)
        }

        fun <T> success(message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(code = 200, message = message, data = data)
        }

        fun <T> error(code: Int, message: String): ApiResponse<T> {
            return ApiResponse(code = code, message = message, data = null)
        }

        fun <T> error(resultCode: ResultCode): ApiResponse<T> {
            return ApiResponse(code = resultCode.code, message = resultCode.message, data = null)
        }
    }
}