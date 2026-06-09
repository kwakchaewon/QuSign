package com.qusign.admin.dto

import com.qusign.auth.entity.User

data class AdminStatsResponse(
    val totalUsers: Long,
    val totalSignatures: Long,
    val pendingSignatures: Long,
    val completedSignatures: Long,
    val cancelledSignatures: Long,
)

data class AdminUserResponse(
    val email: String,
    val role: String,
    val createdAt: String?,
    val disabledAt: String?,
) {
    constructor(user: User) : this(
        email = user.email,
        role = user.role,
        createdAt = user.createdAt?.toString(),
        disabledAt = user.disabledAt?.toString(),
    )
}

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
