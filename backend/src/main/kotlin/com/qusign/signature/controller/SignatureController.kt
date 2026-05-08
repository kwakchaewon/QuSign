package com.qusign.signature.controller

import com.qusign.common.response.ApiResponse
import com.qusign.signature.dto.*
import com.qusign.signature.service.SignatureFlowService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

    @GetMapping("/signature-requests/{token}/signed-document")
    fun getSignedDocument(
        @AuthenticationPrincipal email: String,
        @PathVariable token: String,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getSignedDocument(token, email)
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''$encodedFilename")
        response.outputStream.write(bytes)
    }

    @GetMapping("/signature-requests/{token}/document")
    fun getDocument(
        @AuthenticationPrincipal email: String,
        @PathVariable token: String,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getDocument(token, email)
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''$encodedFilename")
        response.outputStream.write(bytes)
    }

    @PostMapping("/verify")
    fun verify(
        @Valid @RequestBody dto: VerifyRequest,
    ): ApiResponse<VerifyResponse> =
        ApiResponse.ok(signatureFlowService.verify(dto.token))
}
