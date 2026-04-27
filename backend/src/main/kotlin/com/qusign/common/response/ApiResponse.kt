package com.qusign.common.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
) {
    companion object {
        fun <T> ok(data: T) = ApiResponse(success = true, data = data)
        fun ok(message: String) = ApiResponse<Unit>(success = true, message = message)
        fun error(message: String) = ApiResponse<Unit>(success = false, message = message)
    }
}
