package com.qusign.signature.dto

import com.qusign.signature.entity.Signature
import com.qusign.signature.entity.SignatureRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "단건 서명 요청 생성 DTO")
data class CreateSignatureRequestDto(
    @Schema(description = "서명할 문서 ID", example = "1")
    @field:NotNull val documentId: Long,
    @Schema(description = "서명자 이메일", example = "signer@example.com")
    @field:NotBlank @field:Email val signerEmail: String,
    @Schema(description = "서명 만료 시간 (시간 단위, 기본 72시간)", example = "72")
    @field:Min(1) val expirationHours: Long = 72,
    @Schema(description = "서명자에게 전달할 메시지 (최대 1000자)", example = "계약서 서명 부탁드립니다.", nullable = true)
    @field:Size(max = 1000) val message: String? = null,
)

@Schema(description = "배치 서명 요청 생성 DTO (최대 25건)")
data class BatchCreateSignatureRequestDto(
    @Schema(description = "서명 요청 목록 (최대 25건)")
    @field:NotEmpty @field:Size(max = 25) @field:Valid val requests: List<CreateSignatureRequestDto>,
)

@Schema(description = "번들 서명 요청 생성 DTO")
data class CreateBundleSignatureRequestDto(
    @Schema(description = "번들에 포함할 문서 ID 목록 (최대 5개)")
    @field:NotEmpty @field:Size(max = 5) val documentIds: List<Long>,
    @Schema(description = "서명자 이메일 목록 (최대 5명)")
    @field:NotEmpty @field:Size(max = 5) val signerEmails: List<@Email @NotBlank String>,
    @Schema(description = "서명 만료 시간 (시간 단위, 기본 72시간)", example = "72")
    @field:Min(1) val expirationHours: Long = 72,
    @Schema(description = "서명자에게 전달할 메시지 (최대 1000자)", nullable = true)
    @field:Size(max = 1000) val message: String? = null,
)

@Schema(description = "번들 서명 요청 생성 응답")
data class BundleSignatureRequestResponse(
    @Schema(description = "번들 ID", example = "1")
    val bundleId: Long,
    @Schema(description = "서명자별 번들 토큰 목록")
    val signers: List<BundleSignerLinkDto>,
)

@Schema(description = "번들 서명자 토큰 정보")
data class BundleSignerLinkDto(
    @Schema(description = "서명자 이메일", example = "signer@example.com")
    val email: String,
    @Schema(description = "서명자용 번들 토큰 (UUID)")
    val bundleToken: String,
)

@Schema(description = "서명 실행 요청")
data class SignDto(
    @Schema(description = "PQC 개인키 복호화용 비밀번호 (로그인 비밀번호와 동일)", example = "password1")
    @field:NotBlank val password: String,
)

@Schema(description = "토큰 기반 서명 검증 요청")
data class VerifyRequest(
    @Schema(description = "검증할 서명 토큰 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    @field:NotBlank val token: String,
)

@Schema(description = "서명 요청 응답")
data class SignatureRequestResponse(
    @Schema(description = "서명 요청 ID", example = "1")
    val id: Long,
    @Schema(description = "대상 문서 ID", example = "1")
    val documentId: Long,
    @Schema(description = "문서 파일명", example = "contract.pdf")
    val documentFilename: String,
    @Schema(description = "서명자 이메일", example = "signer@example.com")
    val signerEmail: String,
    @Schema(description = "서명 요청 토큰 (UUID)")
    val token: String,
    @Schema(description = "서명 상태", example = "PENDING", allowableValues = ["PENDING", "SIGNED", "CANCELLED", "EXPIRED"])
    val status: String,
    @Schema(description = "만료 일시 (ISO 8601)", example = "2025-01-04T12:00:00")
    val expiresAt: String,
    @Schema(description = "요청 생성 일시 (ISO 8601)", example = "2025-01-01T12:00:00", nullable = true)
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

@Schema(description = "서명 실행 응답")
data class SignatureResponse(
    @Schema(description = "서명 ID", example = "1")
    val id: Long,
    @Schema(description = "연결된 서명 요청 ID", example = "1")
    val signatureRequestId: Long,
    @Schema(description = "서명 완료 일시 (ISO 8601)", example = "2025-01-02T09:30:00", nullable = true)
    val signedAt: String?,
) {
    constructor(sig: Signature) : this(
        id = sig.id,
        signatureRequestId = sig.signatureRequest.id,
        signedAt = sig.signedAt?.toString(),
    )
}

@Schema(description = "서명 검증 결과")
data class VerifyResponse(
    @Schema(description = "서명 유효 여부")
    val valid: Boolean,
    @Schema(description = "서명자 이메일", nullable = true)
    val signerId: String? = null,
    @Schema(description = "서명 완료 일시 (ISO 8601)", nullable = true)
    val signedAt: String? = null,
    @Schema(description = "문서 SHA3-256 해시값", nullable = true)
    val documentHash: String? = null,
    @Schema(description = "RFC 3161 TSA 공인 시각 (UTC). TSA 미설정이거나 타임스탬프가 없으면 null", nullable = true)
    val timestampedAt: String? = null,
    @Schema(description = "타임스탬프를 발급한 TSA URL", nullable = true)
    val tsaUrl: String? = null,
    @Schema(description = "RFC 3161 토큰의 messageImprint 검증 결과. null = 토큰 없음", nullable = true)
    val timestampValid: Boolean? = null,
)

@Schema(description = "서명자 상세 정보")
data class SignerDetailDto(
    @Schema(description = "서명자 이메일", example = "signer@example.com")
    val email: String,
    @Schema(description = "서명 상태", example = "PENDING", allowableValues = ["PENDING", "SIGNED", "CANCELLED", "EXPIRED"])
    val status: String,
    @Schema(description = "서명 완료 일시 (ISO 8601)", nullable = true)
    val signedAt: String?,
    @Schema(description = "단건 서명 토큰", nullable = true)
    val signatureToken: String?,
    @Schema(description = "번들 서명 토큰", nullable = true)
    val bundleToken: String?,
)

@Schema(description = "번들 내 문서 정보")
data class BundleDocumentInfoDto(
    @Schema(description = "번들 내 순서 인덱스 (0부터 시작)", example = "0")
    val index: Int,
    @Schema(description = "파일명", example = "contract.pdf")
    val filename: String,
    @Schema(description = "SHA3-256 해시값 (16진수)")
    val hashSha3256: String,
)

@Schema(description = "서명자에게 반환되는 서명 요청 정보 (단건 또는 번들)")
data class SignerRequestInfoResponse(
    @Schema(description = "문서명 (번들인 경우 대표 문서명)", example = "contract.pdf")
    val documentName: String,
    @Schema(description = "서명 요청자 이메일", example = "requester@example.com")
    val requesterEmail: String,
    @Schema(description = "요청자 메시지", nullable = true)
    val message: String?,
    @Schema(description = "요청 생성 일시 (ISO 8601)", example = "2025-01-01T12:00:00")
    val requestedAt: String,
    @Schema(description = "만료 일시 (ISO 8601)", example = "2025-01-04T12:00:00")
    val expiresAt: String,
    @Schema(description = "문서 SHA3-256 해시값 (단건) 또는 대표 문서 해시")
    val hashSha3256: String,
    @Schema(description = "취소된 요청 여부")
    val cancelled: Boolean,
    @Schema(description = "이미 서명 완료 여부")
    val signed: Boolean,
    @Schema(description = "번들 내 문서 목록 (단건이면 자기 자신 1개)")
    val documents: List<BundleDocumentInfoDto> = emptyList(),
    @Schema(description = "번들 서명 요청 여부")
    val isBundle: Boolean = false,
)

@Schema(description = "서명 요청 상세 응답 (요청자용)")
data class SignatureRequestDetailResponse(
    @Schema(description = "서명 요청 ID", example = "1")
    val id: Long,
    @Schema(description = "문서명", example = "contract.pdf")
    val documentName: String,
    @Schema(description = "문서 SHA3-256 해시값")
    val hashSha3256: String,
    @Schema(description = "문서 업로드 일시 (ISO 8601)")
    val uploadedAt: String,
    @Schema(description = "요청자 이메일", example = "requester@example.com")
    val requesterEmail: String,
    @Schema(description = "서명 알고리즘", example = "PQC_CRYSTALS_DILITHIUM3")
    val algorithm: String,
    @Schema(description = "요청 생성 일시 (ISO 8601)")
    val requestedAt: String,
    @Schema(description = "만료 일시 (ISO 8601)")
    val expiresAt: String,
    @Schema(description = "서명자 목록")
    val signers: List<SignerDetailDto>,
)

@Schema(description = "번들 상세 응답 (요청자용)")
data class BundleDetailResponse(
    @Schema(description = "번들 ID", example = "1")
    val bundleId: Long,
    @Schema(description = "번들 대표 문서명", example = "contract.pdf")
    val primaryDocName: String,
    @Schema(description = "번들 내 문서 수", example = "3")
    val docCount: Int,
    @Schema(description = "번들 내 문서 목록")
    val documents: List<BundleDocumentInfoDto>,
    @Schema(description = "요청자 이메일", example = "requester@example.com")
    val requesterEmail: String,
    @Schema(description = "서명 알고리즘", example = "PQC_CRYSTALS_DILITHIUM3")
    val algorithm: String,
    @Schema(description = "요청 생성 일시 (ISO 8601)")
    val requestedAt: String,
    @Schema(description = "만료 일시 (ISO 8601)")
    val expiresAt: String,
    @Schema(description = "서명자 목록")
    val signers: List<BundleSignerDetailDto>,
)

@Schema(description = "번들 서명자 상세 정보")
data class BundleSignerDetailDto(
    @Schema(description = "서명자 이메일", example = "signer@example.com")
    val email: String,
    @Schema(description = "서명 상태", example = "PENDING", allowableValues = ["SIGNED", "PENDING", "CANCELLED", "EXPIRED"])
    val status: String,
    @Schema(description = "서명 완료 일시 (ISO 8601)", nullable = true)
    val signedAt: String?,
    @Schema(description = "번들 서명 토큰 (PENDING 상태일 때만 반환)", nullable = true)
    val bundleToken: String?,
    @Schema(description = "서명 완료 시 다운로드 가능한 문서 목록")
    val signedDocuments: List<BundleSignedDocDto> = emptyList(),
)

@Schema(description = "번들 서명된 문서 참조")
data class BundleSignedDocDto(
    @Schema(description = "번들 내 인덱스 (0부터 시작)", example = "0")
    val index: Int,
    @Schema(description = "파일명", example = "contract.pdf")
    val filename: String,
)

@Schema(description = "받은 서명 요청 항목")
data class ReceivedDocumentItem(
    @Schema(description = "서명 요청 ID", example = "1")
    val requestId: Long,
    @Schema(description = "문서명", example = "contract.pdf")
    val documentName: String,
    @Schema(description = "요청자 이메일", example = "requester@example.com")
    val requesterEmail: String,
    @Schema(description = "서명 상태", example = "PENDING", allowableValues = ["PENDING", "SIGNED", "CANCELLED", "EXPIRED"])
    val status: String,
    @Schema(description = "단건 서명 토큰")
    val token: String,
    @Schema(description = "번들 서명 토큰 (번들 요청)", nullable = true)
    val bundleToken: String?,
    @Schema(description = "번들 내 문서 수 (단건은 1)", example = "1")
    val bundleDocCount: Int,
    @Schema(description = "만료 일시 (ISO 8601)")
    val expiresAt: String,
    @Schema(description = "요청 생성 일시 (ISO 8601)")
    val createdAt: String,
    @Schema(description = "요청자 메시지", nullable = true)
    val message: String?,
    @Schema(description = "번들 서명 요청 여부")
    val isBundle: Boolean,
)

@Schema(description = "받은 서명 요청 목록 응답")
data class ReceivedDocumentsResponse(
    @Schema(description = "받은 서명 요청 목록")
    val received: List<ReceivedDocumentItem>,
)
