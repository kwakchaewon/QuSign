package com.qusign.admin.dto

import com.qusign.auth.entity.User
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 전체 통계")
data class AdminStatsResponse(
    @Schema(description = "전체 가입 사용자 수", example = "100")
    val totalUsers: Long,
    @Schema(description = "전체 서명 요청 수", example = "500")
    val totalSignatures: Long,
    @Schema(description = "대기 중인 서명 요청 수", example = "50")
    val pendingSignatures: Long,
    @Schema(description = "완료된 서명 요청 수", example = "400")
    val completedSignatures: Long,
    @Schema(description = "취소된 서명 요청 수", example = "50")
    val cancelledSignatures: Long,
)

@Schema(description = "관리자용 사용자 정보")
data class AdminUserResponse(
    @Schema(description = "이메일 주소", example = "user@example.com")
    val email: String,
    @Schema(description = "역할", example = "USER", allowableValues = ["USER", "ADMIN"])
    val role: String,
    @Schema(description = "가입 일시 (ISO 8601)", nullable = true)
    val createdAt: String?,
    @Schema(description = "비활성화 일시 (ISO 8601). null이면 활성 계정", nullable = true)
    val disabledAt: String?,
) {
    constructor(user: User) : this(
        email = user.email,
        role = user.role,
        createdAt = user.createdAt?.toString(),
        disabledAt = user.disabledAt?.toString(),
    )
}

@Schema(description = "페이지 응답")
data class PagedResponse<T>(
    @Schema(description = "현재 페이지 데이터 목록")
    val content: List<T>,
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    val page: Int,
    @Schema(description = "페이지 크기", example = "20")
    val size: Int,
    @Schema(description = "전체 데이터 수", example = "100")
    val totalElements: Long,
    @Schema(description = "전체 페이지 수", example = "5")
    val totalPages: Int,
)
