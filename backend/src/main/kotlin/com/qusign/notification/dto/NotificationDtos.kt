package com.qusign.notification.dto

import com.qusign.notification.entity.Notification
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "알림 항목")
data class NotificationResponse(
    @Schema(description = "알림 ID", example = "1")
    val id: Long,
    @Schema(description = "알림 타입", example = "SIGN_REQUEST")
    val type: String,
    @Schema(description = "알림 제목", example = "서명 요청이 도착했습니다")
    val title: String,
    @Schema(description = "알림 내용", example = "requester@example.com님이 서명을 요청했습니다.")
    val message: String,
    @Schema(description = "연관 문서/요청 ID", nullable = true, example = "1")
    val referenceId: Long?,
    @Schema(description = "연관 서명 토큰", nullable = true)
    val referenceToken: String?,
    @Schema(description = "읽음 여부")
    val isRead: Boolean,
    @Schema(description = "알림 생성 일시 (ISO 8601)", example = "2025-01-01T12:00:00")
    val createdAt: String,
) {
    constructor(n: Notification) : this(
        id = n.id,
        type = n.type,
        title = n.title,
        message = n.message,
        referenceId = n.referenceId,
        referenceToken = n.referenceToken,
        isRead = n.isRead,
        createdAt = n.createdAt?.toString() ?: "",
    )
}

@Schema(description = "미읽음 알림 수")
data class UnreadCountResponse(
    @Schema(description = "미읽음 알림 개수", example = "3")
    val count: Long,
)

@Schema(description = "SSE 단기 토큰 응답")
data class SseTokenResponse(
    @Schema(description = "SSE 연결용 단기 토큰 (30초 유효, 일회성)")
    val sseToken: String,
)
