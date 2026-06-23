package com.qusign.signature.controller

import com.qusign.common.audit.AuditContextExtractor
import com.qusign.common.response.ApiResponse
import com.qusign.signature.dto.*
import com.qusign.signature.service.SignatureFlowService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "Signature", description = "서명 요청 생성 · 서명 실행 · 검증 · 문서 다운로드")
@RestController
@RequestMapping("/api")
class SignatureController(
    private val signatureFlowService: SignatureFlowService,
    private val auditContextExtractor: AuditContextExtractor,
) {

    // ── 받은 문서 목록 (서명자용) ─────────────────────────────────────────────

    @Operation(summary = "[서명자] 받은 서명 요청 목록", description = "내가 서명자로 지정된 서명 요청 목록을 반환합니다.")
    @GetMapping("/signature-requests/received")
    fun getReceivedDocuments(
        @AuthenticationPrincipal email: String,
    ): ApiResponse<ReceivedDocumentsResponse> =
        ApiResponse.ok(signatureFlowService.getReceivedDocuments(email))

    // ── 단건 서명 요청 ────────────────────────────────────────────────────────

    @Operation(summary = "[요청자] 단건 서명 요청 생성", description = "문서 1개에 서명자 1명을 지정하여 서명을 요청합니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "서명 요청 생성 성공"),
        SwaggerApiResponse(responseCode = "400", description = "문서 ID 오류 또는 유효성 검증 실패"),
        SwaggerApiResponse(responseCode = "403", description = "본인 문서가 아님"),
        SwaggerApiResponse(responseCode = "404", description = "문서를 찾을 수 없음"),
    )
    @PostMapping("/signature-requests")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRequest(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody dto: CreateSignatureRequestDto,
        request: HttpServletRequest,
    ): ApiResponse<SignatureRequestResponse> =
        ApiResponse.ok(signatureFlowService.requestSignature(email, dto, auditContextExtractor.from(request)))

    @Operation(
        summary = "[요청자] 배치 서명 요청 생성",
        description = "여러 (문서, 서명자) 쌍을 한 번에 서명 요청합니다. 최대 25건."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "배치 서명 요청 생성 성공"),
        SwaggerApiResponse(responseCode = "400", description = "25건 초과 또는 유효성 검증 실패"),
    )
    @PostMapping("/signature-requests/batch")
    @ResponseStatus(HttpStatus.CREATED)
    fun createRequestBatch(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody dto: BatchCreateSignatureRequestDto,
        request: HttpServletRequest,
    ): ApiResponse<List<SignatureRequestResponse>> =
        ApiResponse.ok(signatureFlowService.requestSignatureBatch(email, dto, auditContextExtractor.from(request)))

    // ── 번들 서명 요청 ────────────────────────────────────────────────────────

    @Operation(
        summary = "[요청자] 번들 서명 요청 생성",
        description = "문서 최대 5개 × 서명자 최대 5명의 묶음 서명을 요청합니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "번들 서명 요청 생성 성공"),
        SwaggerApiResponse(responseCode = "400", description = "문서 또는 서명자 수 초과"),
    )
    @PostMapping("/signature-requests/bundle")
    @ResponseStatus(HttpStatus.CREATED)
    fun createBundleRequest(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody dto: CreateBundleSignatureRequestDto,
        request: HttpServletRequest,
    ): ApiResponse<BundleSignatureRequestResponse> =
        ApiResponse.ok(signatureFlowService.requestBundleSignature(email, dto, auditContextExtractor.from(request)))

    // ── 번들 상세 (요청자용) ──────────────────────────────────────────────────

    @Operation(summary = "[요청자] 번들 상세 조회", description = "번들 ID로 번들 내 문서 목록과 서명자별 상태를 조회합니다.")
    @GetMapping("/bundles/{bundleId}")
    fun getBundleDetail(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "번들 ID", required = true, example = "1")
        @PathVariable bundleId: Long,
    ): ApiResponse<BundleDetailResponse> =
        ApiResponse.ok(signatureFlowService.getBundleDetail(bundleId, email))

    // ── 번들 서명자 취소 (요청자용) ───────────────────────────────────────────

    @Operation(summary = "[요청자] 번들 서명자 취소", description = "번들 내 특정 서명자의 서명 요청을 취소합니다.")
    @PostMapping("/bundles/{bundleId}/signers/{signerEmail}/cancel")
    fun cancelBundleSigner(
        @AuthenticationPrincipal requesterEmail: String,
        @Parameter(description = "번들 ID", required = true, example = "1")
        @PathVariable bundleId: Long,
        @Parameter(description = "취소할 서명자 이메일", required = true)
        @PathVariable signerEmail: String,
        request: HttpServletRequest,
    ): ApiResponse<Unit> {
        signatureFlowService.cancelBundleSigner(bundleId, signerEmail, requesterEmail, auditContextExtractor.from(request))
        return ApiResponse.ok(Unit)
    }

    // ── 번들 서명된 PDF 다운로드 (요청자용) ──────────────────────────────────

    @Operation(
        summary = "[요청자] 번들 서명된 문서 다운로드",
        description = "번들 내 특정 서명자가 서명한 특정 문서를 다운로드합니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "서명된 PDF", content = [Content(mediaType = "application/pdf")]),
        SwaggerApiResponse(responseCode = "404", description = "서명된 문서를 찾을 수 없음"),
    )
    @GetMapping("/bundles/{bundleId}/signers/{signerEmail}/signed-documents/{docIndex}")
    fun downloadBundleSignedDoc(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "번들 ID", required = true, example = "1")
        @PathVariable bundleId: Long,
        @Parameter(description = "서명자 이메일", required = true)
        @PathVariable signerEmail: String,
        @Parameter(description = "번들 내 문서 인덱스 (0부터 시작)", required = true, example = "0")
        @PathVariable docIndex: Int,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getBundleSignedDocByRequester(bundleId, signerEmail, docIndex, email, auditContextExtractor.from(request))
        sendPdf(response, bytes, filename)
    }

    // ── 서명자용 공통 ─────────────────────────────────────────────────────────

    @Operation(
        summary = "[서명자] 서명 요청 정보 조회",
        description = "서명 링크의 토큰으로 서명 요청 정보(문서명, 요청자, 만료일 등)를 조회합니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "서명 요청 정보"),
        SwaggerApiResponse(responseCode = "404", description = "토큰을 찾을 수 없음"),
    )
    @GetMapping("/signature-requests/{token}/info")
    fun getSignerRequestInfo(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "서명 요청 토큰 (UUID)", required = true)
        @PathVariable token: String,
    ): ApiResponse<SignerRequestInfoResponse> =
        ApiResponse.ok(signatureFlowService.getSignerRequestInfo(token, email))

    @Operation(
        summary = "[서명자] 서명 실행",
        description = "PQC 개인키를 비밀번호로 복호화하여 서명합니다. RFC 3161 타임스탬프가 자동 첨부됩니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "서명 성공"),
        SwaggerApiResponse(responseCode = "400", description = "비밀번호 오류 또는 이미 서명됨"),
        SwaggerApiResponse(responseCode = "410", description = "서명 요청 만료"),
    )
    @PostMapping("/signature-requests/{token}/sign")
    fun sign(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "서명 요청 토큰 (UUID)", required = true)
        @PathVariable token: String,
        @Valid @RequestBody dto: SignDto,
        request: HttpServletRequest,
    ): ApiResponse<SignatureResponse> =
        ApiResponse.ok(signatureFlowService.sign(token, email, dto.password, auditContextExtractor.from(request)))

    @Operation(summary = "[서명자] 서명 전 원본 문서 조회", description = "서명 전 원본 PDF를 미리 볼 수 있습니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "원본 PDF", content = [Content(mediaType = "application/pdf")]),
    )
    @GetMapping("/signature-requests/{token}/document")
    fun getDocument(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "서명 요청 토큰 (UUID)", required = true)
        @PathVariable token: String,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getDocument(token, email)
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''$encodedFilename")
        response.outputStream.write(bytes)
    }

    @Operation(summary = "[서명자] 번들 원본 문서 조회", description = "번들 서명 요청 중 특정 인덱스 문서를 미리 봅니다.")
    @GetMapping("/signature-requests/{bundleToken}/bundle-documents/{index}")
    fun getBundleDocument(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "번들 서명 토큰 (UUID)", required = true)
        @PathVariable bundleToken: String,
        @Parameter(description = "번들 내 문서 인덱스 (0부터 시작)", required = true, example = "0")
        @PathVariable index: Int,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getBundleDocument(bundleToken, email, index)
        val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")
        response.contentType = "application/pdf"
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''$encodedFilename")
        response.outputStream.write(bytes)
    }

    @Operation(
        summary = "[서명자] 서명된 문서 조회",
        description = "자신이 서명한 PDF(PQC 서명 + RFC 3161 타임스탬프 포함)를 다운로드합니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "서명된 PDF", content = [Content(mediaType = "application/pdf")]),
        SwaggerApiResponse(responseCode = "404", description = "아직 서명되지 않음"),
    )
    @GetMapping("/signature-requests/{token}/signed-document")
    fun getSignedDocument(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "서명 요청 토큰 (UUID)", required = true)
        @PathVariable token: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getSignedDocument(token, email, auditContextExtractor.from(request))
        sendPdf(response, bytes, filename)
    }

    @Operation(summary = "[서명자] 번들 서명된 문서 조회", description = "번들 서명 후 특정 인덱스 문서를 다운로드합니다.")
    @GetMapping("/signature-requests/{bundleToken}/signed-bundle-documents/{index}")
    fun getSignedBundleDocument(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "번들 서명 토큰 (UUID)", required = true)
        @PathVariable bundleToken: String,
        @Parameter(description = "번들 내 문서 인덱스 (0부터 시작)", required = true, example = "0")
        @PathVariable index: Int,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getSignedBundleDocument(bundleToken, email, index, auditContextExtractor.from(request))
        sendPdf(response, bytes, filename)
    }

    // ── 요청자용 ─────────────────────────────────────────────────────────────

    @Operation(summary = "[요청자] 서명 요청 상세 조회", description = "서명 요청 ID로 서명자별 상태를 조회합니다.")
    @GetMapping("/signature-requests/{id}")
    fun getDetail(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "서명 요청 ID", required = true, example = "1")
        @PathVariable id: Long,
    ): ApiResponse<SignatureRequestDetailResponse> =
        ApiResponse.ok(signatureFlowService.getDetail(id, email))

    @Operation(
        summary = "[요청자] 서명된 문서 다운로드",
        description = "요청자 입장에서 특정 서명자가 서명한 문서를 다운로드합니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "서명된 PDF", content = [Content(mediaType = "application/pdf")]),
        SwaggerApiResponse(responseCode = "404", description = "아직 서명되지 않음"),
    )
    @GetMapping("/signature-requests/{id}/download")
    fun downloadSigned(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "서명 요청 ID", required = true, example = "1")
        @PathVariable id: Long,
        @Parameter(description = "서명자 이메일", required = true)
        @RequestParam signerEmail: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val (bytes, filename) = signatureFlowService.getSignedDocumentByRequester(id, signerEmail, email, auditContextExtractor.from(request))
        sendPdf(response, bytes, filename)
    }

    @Operation(summary = "[요청자] 서명자 취소", description = "단건 서명 요청에서 특정 서명자의 요청을 취소합니다.")
    @PostMapping("/signature-requests/{id}/signers/{email}/cancel")
    fun cancelSigner(
        @AuthenticationPrincipal requesterEmail: String,
        @Parameter(description = "서명 요청 ID", required = true, example = "1")
        @PathVariable id: Long,
        @Parameter(description = "취소할 서명자 이메일", required = true)
        @PathVariable email: String,
        request: HttpServletRequest,
    ): ApiResponse<Unit> {
        signatureFlowService.cancelSigner(id, email, requesterEmail, auditContextExtractor.from(request))
        return ApiResponse.ok(Unit)
    }

    // ── 검증 ─────────────────────────────────────────────────────────────────

    @Operation(
        summary = "토큰으로 서명 검증",
        description = "서명 토큰으로 PQC 서명 유효성과 RFC 3161 타임스탬프를 검증합니다. 인증 불필요."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "검증 결과 (valid 필드로 유효 여부 확인)"),
    )
    @SecurityRequirements
    @PostMapping("/verify")
    fun verify(
        @Valid @RequestBody dto: VerifyRequest,
    ): ApiResponse<VerifyResponse> =
        ApiResponse.ok(signatureFlowService.verify(dto.token))

    @Operation(
        summary = "PDF 파일로 서명 검증",
        description = "서명된 PDF 파일을 직접 업로드하여 PQC 서명과 타임스탬프를 검증합니다. 인증 불필요."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "검증 결과", content = [Content(mediaType = "application/json")]),
        SwaggerApiResponse(responseCode = "400", description = "PDF 형식 오류"),
    )
    @SecurityRequirements
    @PostMapping("/verify/file", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun verifyFile(
        @Parameter(description = "검증할 서명된 PDF 파일", required = true)
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
