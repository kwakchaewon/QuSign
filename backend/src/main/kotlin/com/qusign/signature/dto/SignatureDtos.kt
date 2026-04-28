package com.qusign.signature.dto

import com.qusign.signature.entity.Signature
import com.qusign.signature.entity.SignatureRequest
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateSignatureRequestDto(
    @field:NotNull val documentId: Long,
    @field:NotBlank @field:Email val signerEmail: String,
    @field:Min(1) val expirationHours: Long = 72,
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
)
