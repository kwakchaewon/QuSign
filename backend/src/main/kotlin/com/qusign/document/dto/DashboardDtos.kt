package com.qusign.document.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "대시보드 전체 응답")
data class DashboardResponse(
    @Schema(description = "총 문서 수", example = "10")
    val totalDocuments: Int,
    @Schema(description = "서명 완료 건수", example = "5")
    val signedCount: Int,
    @Schema(description = "서명 대기 건수", example = "3")
    val pendingCount: Int,
    @Schema(description = "만료된 서명 요청 건수", example = "2")
    val expiredCount: Int,
    @Schema(description = "최근 서명 요청 목록")
    val recentRequests: List<RecentRequestItem>,
)

@Schema(description = "최근 서명 요청 항목")
data class RecentRequestItem(
    @Schema(description = "서명 요청 ID", example = "1")
    val id: Long,
    @Schema(description = "문서명", example = "contract.pdf")
    val documentName: String,
    @Schema(description = "서명자 이메일 목록")
    val signers: List<String>,
    @Schema(description = "요청 생성 일시 (ISO 8601)", example = "2025-01-01T12:00:00")
    val createdAt: String,
    @Schema(description = "서명 상태", example = "PENDING", allowableValues = ["PENDING", "SIGNED", "CANCELLED", "EXPIRED"])
    val status: String,
)

@Schema(description = "대시보드 요약 통계")
data class DashboardSummaryResponse(
    @Schema(description = "내가 보낸 요청 중 대기 중인 건수", example = "3")
    val sentPending: Int,
    @Schema(description = "내가 보낸 요청 중 완료된 건수", example = "5")
    val sentSigned: Int,
    @Schema(description = "내가 받아서 아직 서명하지 않은 건수", example = "2")
    val receivedUnsigned: Int,
)

@Schema(description = "즉시 처리가 필요한 액션 아이템")
data class DashboardActionItem(
    @Schema(description = "방향", example = "SENT", allowableValues = ["SENT", "RECEIVED"])
    val direction: String,
    @Schema(description = "문서명", example = "contract.pdf")
    val docName: String,
    @Schema(description = "상대방 이메일 (SENT: 서명자, RECEIVED: 요청자)", example = "signer@example.com")
    val counterpart: String,
    @Schema(description = "요청 생성 일시 (ISO 8601)", example = "2025-01-01T12:00:00")
    val createdAt: String,
    @Schema(description = "서명 페이지 경로", example = "/sign/abc123")
    val signPath: String,
    @Schema(description = "단건 서명 토큰 (단건 요청)", nullable = true)
    val token: String? = null,
    @Schema(description = "번들 서명 토큰 (번들 요청)", nullable = true)
    val bundleToken: String? = null,
    @Schema(description = "번들 ID (번들 요청)", nullable = true)
    val bundleId: Long? = null,
)

@Schema(description = "액션 아이템 목록 응답")
data class DashboardActionItemsResponse(
    @Schema(description = "처리 필요한 액션 아이템 목록")
    val items: List<DashboardActionItem>,
)
