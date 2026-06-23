package com.qusign.document.dto

import com.qusign.document.entity.Document
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "문서 응답")
data class DocumentResponse(
    @Schema(description = "문서 ID", example = "1")
    val id: Long,
    @Schema(description = "원본 파일명", example = "contract.pdf")
    val originalFilename: String,
    @Schema(description = "SHA3-256 해시값 (16진수)", example = "a1b2c3d4...")
    val hashSha3256: String,
    @Schema(description = "업로드 일시 (ISO 8601)", example = "2025-01-01T12:00:00")
    val createdAt: String?,
    @Schema(description = "서명 상태", example = "NONE", allowableValues = ["NONE", "PENDING", "SIGNED"])
    val signatureStatus: String = "NONE",
    @Schema(description = "번들 ID (번들 소속 시)", example = "1", nullable = true)
    val bundleId: Long? = null,
    @Schema(description = "번들 대표 문서 여부")
    val bundlePrimary: Boolean = false,
    @Schema(description = "번들 내 문서 수 (단독 문서는 1)", example = "1")
    val bundleDocCount: Int = 1,
) {
    constructor(doc: Document, signatureStatus: String = "NONE") : this(
        id = doc.id,
        originalFilename = doc.originalFilename,
        hashSha3256 = doc.hashSha3256,
        createdAt = doc.createdAt?.toString(),
        signatureStatus = signatureStatus,
    )
}
