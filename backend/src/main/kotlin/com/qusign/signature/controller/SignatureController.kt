package com.qusign.signature.controller

import com.qusign.common.response.ApiResponse
import com.qusign.signature.dto.*
import com.qusign.signature.service.SignatureFlowService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class SignatureController(private val signatureFlowService: SignatureFlowService) {

    @PostMapping("/signature-requests")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRequest(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody dto: CreateSignatureRequestDto,
    ): ApiResponse<SignatureRequestResponse> =
        ApiResponse.ok(signatureFlowService.requestSignature(email, dto))

    @PostMapping("/signature-requests/{token}/sign")
    fun sign(
        @AuthenticationPrincipal email: String,
        @PathVariable token: String,
        @Valid @RequestBody dto: SignDto,
    ): ApiResponse<SignatureResponse> =
        ApiResponse.ok(signatureFlowService.sign(token, email, dto.password))

    @PostMapping("/verify")
    fun verify(
        @Valid @RequestBody dto: VerifyRequest,
    ): ApiResponse<VerifyResponse> =
        ApiResponse.ok(signatureFlowService.verify(dto.token))
}
