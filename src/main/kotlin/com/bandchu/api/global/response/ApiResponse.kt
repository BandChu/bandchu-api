package com.bandchu.api.global.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(true, data, "요청이 성공적으로 처리되었습니다.")
        }

        fun <T> success(data: T, message: String): ApiResponse<T> {
            return ApiResponse(true, data, message)
        }

        fun <T> success(message: String): ApiResponse<T> {
            return ApiResponse(true, null, message)
        }
    }
}