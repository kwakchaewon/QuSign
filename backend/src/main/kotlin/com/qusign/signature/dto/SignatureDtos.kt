package com.qusign.signature.dto

import com.qusign.signature.entity.Signature
import com.qusign.signature.entity.SignatureRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreateSignatureRequestDto(
    @field:NotNull val documentId: Long,
    @field:NotBlank @field:Email val signerEmail: String,
    @field:Min(1) val expirationHours: Long = 72,
    @field:Size(max = 1000) val message: String? = null,
)

data class BatchCreateSignatureRequestDto(
    @field:NotEmpty @field:Size(max = 25) @field:Valid val requests: List<CreateSignatureRequestDto>,
)

// 번들 서명 요청 생성 DTO
data class CreateBundleSignatureRequestDto(
    @field:NotEmpty @field:Size(max = 5) val documentIds: List<Long>,
    @field:NotEmpty @field:Size(max = 5) val signerEmails: List<@Email @NotBlank String>,
    @field:Min(1) val expirationHours: Long = 72,
    @field:Size(max = 1000) val message: String? = null,
)

// 번들 서명 요청 생성 응답
data class BundleSignatureRequestResponse(
    val bundleId: Long,
    val signers: List<BundleSignerLinkDto>,
)

data class BundleSignerLinkDto(
    val email: String,
    val bundleToken: String,
)

data class SignDto(
    @field:NotBlank val password: String,
)

data class VerifyRequest(
    @field:NotBlank val token: String,
)

data class SignatureRequestResponse(
    val id: Long,
    val documentId: Long,
    val documentFilename: String,
    val signerEmail: String,
    val token: String,
    val status: String,
    val expiresAt: String,
    val createdAt: String?,
) {
    constructor(req: SignatureRequest) : this(
        id = req.id,
        documentId = req.document.id,
        documentFilename = req.document.originalFilename,
        signerEmail = req.signerEmail,
        token = req.token,
        status = req.status,
        expiresAt = req.expiresAt.toString(),
        createdAt = req.createdAt?.toString(),
    )
}

data class SignatureResponse(
    val id: Long,
    val signatureRequestId: Long,
    val signedAt: String?,
) {
    constructor(sig: Signature) : this(
        id = sig.id,
        signatureRequestId = sig.signatureRequest.id,
        signedAt = sig.signedAt?.toString(),
    )
}

data class VerifyResponse(
    val valid: Boolean,
    val signerId: String? = null,
    val signedAt: String? = null,
    val documentHash: String? = null,
    /** RFC 3161 TSA 공인 시각 (UTC). TSA 미설정이거나 타임스탬프가 없으면 null */
    val timestampedAt: String? = null,
    /** 타임스탬프를 발급한 TSA URL */
    val tsaUrl: String? = null,
    /** token의 messageImprint가 서명된 PDF 해시와 일치하는지 여부. null = 토큰 없음 */
    val timestampValid: Boolean? = null,
)

data class SignerDetailDto(
    val email: String,
    val status: String,
    val signedAt: String?,
    val signatureToken: String?,
    val bundleToken: String?,
)

// 서명자용 단일 문서 정보
data class BundleDocumentInfoDto(
    val index: Int,
    val filename: String,
    val hashSha3256: String,
)

// 서명자에게 반환되는 서명 요청 정보 (단건 또는 번들)
data class SignerRequestInfoResponse(
    val documentName: String,
    val requesterEmail: String,
    val message: String?,
    val requestedAt: String,
    val expiresAt: String,
    val hashSha3256: String,
    val cancelled: Boolean,
    val signed: Boolean,
    // 번들인 경우: 모든 문서 목록 (단건이면 이 필드에 자기 자신 1개만 들어있음)
    val documents: List<BundleDocumentInfoDto> = emptyList(),
    val isBundle: Boolean = false,
)

data class SignatureRequestDetailResponse(
    val id: Long,
    val documentName: String,
    val hashSha3256: String,
    val uploadedAt: String,
    val requesterEmail: String,
    val algorithm: String,
    val requestedAt: String,
    val expiresAt: String,
    val signers: List<SignerDetailDto>,
)

// 번들 상세 응답 (요청자용)
data class BundleDetailResponse(
    val bundleId: Long,
    val primaryDocName: String,
    val docCount: Int,
    val documents: List<BundleDocumentInfoDto>,
    val requesterEmail: String,
    val algorithm: String,
    val requestedAt: String,
    val expiresAt: String,
    val signers: List<BundleSignerDetailDto>,
)

data class BundleSignerDetailDto(
    val email: String,
    val status: String,         // SIGNED | PENDING | CANCELLED | EXPIRED
    val signedAt: String?,
    val bundleToken: String?,   // PENDING 상태일 때만 반환
    // 서명 완료 시 다운로드 가능한 문서 목록
    val signedDocuments: List<BundleSignedDocDto> = emptyList(),
)

data class BundleSignedDocDto(
    val index: Int,
    val filename: String,
)

// 받은 문서 목록 (서명자용)
data class ReceivedDocumentItem(
    val requestId: Long,
    val documentName: String,
    val requesterEmail: String,
    val status: String,         // PENDING | SIGNED | CANCELLED | EXPIRED
    val token: String,
    val bundleToken: String?,
    val bundleDocCount: Int,
    val expiresAt: String,
    val createdAt: String,
    val message: String?,
    val isBundle: Boolean,
)

data class ReceivedDocumentsResponse(
    val received: List<ReceivedDocumentItem>,
)
