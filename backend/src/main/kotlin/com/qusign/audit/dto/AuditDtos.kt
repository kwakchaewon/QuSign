package com.qusign.audit.dto

import com.qusign.audit.entity.AuditEventType
import com.qusign.audit.entity.AuditLog
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "감사 로그 항목")
data class AuditLogResponse(
    @Schema(description = "감사 로그 ID", example = "1")
    val id: Long,
    @Schema(
        description = "이벤트 타입",
        example = "SIGNED",
        allowableValues = [
            "SIGN_REQUEST_CREATED", "BUNDLE_REQUEST_CREATED",
            "SIGNED", "BUNDLE_SIGNED",
            "SIGNER_CANCELLED", "BUNDLE_SIGNER_CANCELLED",
            "SIGNED_DOCUMENT_DOWNLOADED", "TIMESTAMP_OBTAINED"
        ]
    )
    val eventType: AuditEventType,
    @Schema(description = "이벤트 발생 주체 이메일", example = "user@example.com")
    val actorEmail: String,
    @Schema(description = "요청 IP 주소", example = "192.168.1.1")
    val ipAddress: String,
    @Schema(description = "이벤트 발생 일시 (ISO 8601)", example = "2025-01-01T12:00:00")
    val createdAt: String,
    @Schema(description = "연관된 서명 요청 ID", nullable = true, example = "1")
    val signatureRequestId: Long?,
    @Schema(description = "연관된 번들 ID", nullable = true, example = "1")
    val bundleId: Long?,
    @Schema(description = "연관된 문서 ID", nullable = true, example = "1")
    val documentId: Long?,
) {
    constructor(log: AuditLog) : this(
        id = log.id,
        eventType = log.eventType,
        actorEmail = log.actorEmail,
        ipAddress = log.ipAddress,
        createdAt = log.createdAt.toString(),
        signatureRequestId = log.signatureRequestId,
        bundleId = log.bundleId,
        documentId = log.documentId,
    )
}
