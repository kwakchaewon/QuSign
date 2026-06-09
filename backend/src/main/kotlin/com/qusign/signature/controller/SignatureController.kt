package com.qusign.signature.controller

import com.qusign.common.audit.AuditContext
import com.qusign.common.response.ApiResponse
import com.qusign.signature.dto.*
import com.qusign.signature.service.SignatureFlowService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api")
class SignatureController(private val signatureFlowService: SignatureFlowService) {

    // ── 받은 문서 목록 (서명자용) ─────────────────────────────────────────────
    @GetMapping("/signature-requests/received")
    fun getReceivedDocuments(
        @AuthenticationPrincipal email: String,
    ): ApiResponse<ReceivedDocumentsResponse> =
        ApiResponse.ok(signatureFlowService.getReceivedDocuments(email))

    // ── 단건 서명 요청 ────────────────────────────────────────────────────────
    @PostMapping("/signature-requests")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRequest(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody dto: CreateSignatureRequestDto,
        request: HttpServletRequest,
    ): ApiResponse<SignatureRequestResponse> =
        ApiResponse.ok(signatureFlowService.requestSignature(email, dto, request.auditContext()))

    @PostMapping("/signature-requests/batch")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRequestBatch(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody dto: BatchCreateSignatureRequestDto,
        request: HttpServletRequest,
    ): ApiResponse<List<SignatureRequestResponse>> =
        ApiResponse.ok(signatureFlowService.requestSignatureBatch(email, dto, request.auditContext()))

    // ── 번들 서명 요청 ────────────────────────────────────────────────────────
    @PostMapping("/signature-requests/bundle")
    @ResponseStatus(HttpStatus.CREATED)
    fun createBundleRequest(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody dto: CreateBundleSignatureRequestDto,
        request: HttpServletRequest,
    ): ApiResponse<BundleSignatureRequestResponse> =
        ApiResponse.ok(signatureFlowService.requestBundleSignature(email, dto, request.auditContext()))

    // ── 번들 상세 (요청자용) ──────────────────────────────────────────────────
    @GetMapping("/bundles/{bundleId}")
    fun getBundleDetail(
        @AuthenticationPrincipal email: String,
        @PathVariable bundleId: Long,
    ): ApiResponse<BundleDetailResponse> =
        ApiResponse.ok(signatureFlowService.getBundleDetail(bundleId, email))

    // ── 번들 서명자 취소 (요청자용) ───────────────────────────────────────────
    @PostMapping("/bundles/{bundleId}/signers/{signerEmail}/cancel")
    fun cancelBundleSigner(
        @AuthenticationPrincipal requesterEmail: String,
        @PathVariable bundleId: Long,
        @PathVariable signerEmail: String,
        request: HttpServletRequest,
    ): ApiResponse<Unit> {
        signatureFlowService.cancelBundleSigner(bundleId, signerEmail, requesterEmail, request.auditContext())
        return ApiResponse.ok(Unit)
    }

    // ── 번들 서명된 PDF 다운로드 (요청자용) ──────────────────────────────────
    @GetMapping("/bundles/{bundleId}/signers/{signerEmail}/signed-documents/{docIndex}")
    fun downloadBundleSignedDoc(
        @AuthenticationPrincipal email: String,
        @PathVariable bundleId: Long,
        @PathVariable signerEmail: String,
        @PathVariable docIndex: Int,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getBundleSignedDocByRequester(bundleId, signerEmail, docIndex, email, request.auditContext())
        sendPdf(response, bytes, filename)
    }

    // ── 서명자용 공통 ─────────────────────────────────────────────────────────
    @GetMapping("/signature-requests/{token}/info")
    fun getSignerRequestInfo(
        @AuthenticationPrincipal email: String,
        @PathVariable token: String,
    ): ApiResponse<SignerRequestInfoResponse> =
        ApiResponse.ok(signatureFlowService.getSignerRequestInfo(token, email))

    @PostMapping("/signature-requests/{token}/sign")
    fun sign(
        @AuthenticationPrincipal email: String,
        @PathVariable token: String,
        @Valid @RequestBody dto: SignDto,
        request: HttpServletRequest,
    ): ApiResponse<SignatureResponse> =
        ApiResponse.ok(signatureFlowService.sign(token, email, dto.password, request.auditContext()))

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

    @GetMapping("/signature-requests/{bundleToken}/bundle-documents/{index}")
    fun getBundleDocument(
        @AuthenticationPrincipal email: String,
        @PathVariable bundleToken: String,
        @PathVariable index: Int,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getBundleDocument(bundleToken, email, index)
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''$encodedFilename")
        response.outputStream.write(bytes)
    }

    @GetMapping("/signature-requests/{token}/signed-document")
    fun getSignedDocument(
        @AuthenticationPrincipal email: String,
        @PathVariable token: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getSignedDocument(token, email, request.auditContext())
        sendPdf(response, bytes, filename)
    }

    @GetMapping("/signature-requests/{bundleToken}/signed-bundle-documents/{index}")
    fun getSignedBundleDocument(
        @AuthenticationPrincipal email: String,
        @PathVariable bundleToken: String,
        @PathVariable index: Int,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getSignedBundleDocument(bundleToken, email, index, request.auditContext())
        sendPdf(response, bytes, filename)
    }

    // ── 요청자용 ─────────────────────────────────────────────────────────────
    @GetMapping("/signature-requests/{id}")
    fun getDetail(
        @AuthenticationPrincipal email: String,
        @PathVariable id: Long,
    ): ApiResponse<SignatureRequestDetailResponse> =
        ApiResponse.ok(signatureFlowService.getDetail(id, email))

    @GetMapping("/signature-requests/{id}/download")
    fun downloadSigned(
        @AuthenticationPrincipal email: String,
        @PathVariable id: Long,
        @RequestParam signerEmail: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getSignedDocumentByRequester(id, signerEmail, email, request.auditContext())
        sendPdf(response, bytes, filename)
    }

    @PostMapping("/signature-requests/{id}/signers/{email}/cancel")
    fun cancelSigner(
        @AuthenticationPrincipal requesterEmail: String,
        @PathVariable id: Long,
        @PathVariable email: String,
        request: HttpServletRequest,
    ): ApiResponse<Unit> {
        signatureFlowService.cancelSigner(id, email, requesterEmail, request.auditContext())
        return ApiResponse.ok(Unit)
    }

    // ── 검증 ─────────────────────────────────────────────────────────────────
    @PostMapping("/verify")
    fun verify(
        @Valid @RequestBody dto: VerifyRequest,
    ): ApiResponse<VerifyResponse> =
        ApiResponse.ok(signatureFlowService.verify(dto.token))

    @PostMapping("/verify/file", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun verifyFile(
        @RequestParam("file") file: MultipartFile,
    ): ApiResponse<VerifyResponse> =
        ApiResponse.ok(signatureFlowService.verifyFile(file.bytes))

    // ── helper ────────────────────────────────────────────────────────────────
    private fun sendPdf(response: HttpServletResponse, bytes: ByteArray, filename: String) {
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''$encodedFilename")
        response.outputStream.write(bytes)
    }
}

private fun HttpServletRequest.auditContext(): AuditContext {
    val ip = (getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
        ?: remoteAddr).take(45)
    val ua = (getHeader("User-Agent") ?: "").take(500)
    return AuditContext(ipAddress = ip, userAgent = ua)
}
