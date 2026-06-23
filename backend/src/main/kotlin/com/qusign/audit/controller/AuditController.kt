package com.qusign.audit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.audit.dto.AuditLogResponse
import com.qusign.audit.service.AuditLogService
import com.qusign.common.response.ApiResponse
import com.qusign.signature.service.SignatureFlowService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Audit", description = "감사 로그 조회 · 내보내기")
@RestController
@RequestMapping("/api")
class AuditController(
    private val auditLogService: AuditLogService,
    private val signatureFlowService: SignatureFlowService,
    private val objectMapper: ObjectMapper,
) {

    @Operation(summary = "문서 감사 로그 조회", description = "문서 ID로 해당 문서의 모든 이벤트 감사 로그를 반환합니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "감사 로그 목록"),
        SwaggerApiResponse(responseCode = "403", description = "본인 문서가 아님"),
        SwaggerApiResponse(responseCode = "404", description = "문서를 찾을 수 없음"),
    )
    @GetMapping("/documents/{documentId}/audit")
    fun getDocumentAudit(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "문서 ID", required = true, example = "1")
        @PathVariable documentId: Long,
    ): ApiResponse<List<AuditLogResponse>> {
        signatureFlowService.getDetail(documentId, email)
        return ApiResponse.ok(auditLogService.findByDocumentId(documentId).map { AuditLogResponse(it) })
    }

    @Operation(summary = "번들 감사 로그 조회", description = "번들 ID로 해당 번들의 모든 이벤트 감사 로그를 반환합니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "감사 로그 목록"),
        SwaggerApiResponse(responseCode = "403", description = "본인 번들이 아님"),
        SwaggerApiResponse(responseCode = "404", description = "번들을 찾을 수 없음"),
    )
    @GetMapping("/bundles/{bundleId}/audit")
    fun getBundleAudit(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "번들 ID", required = true, example = "1")
        @PathVariable bundleId: Long,
    ): ApiResponse<List<AuditLogResponse>> {
        signatureFlowService.getBundleDetail(bundleId, email)
        return ApiResponse.ok(auditLogService.findByBundleId(bundleId).map { AuditLogResponse(it) })
    }

    @Operation(
        summary = "문서 감사 로그 내보내기",
        description = "문서의 감사 로그를 JSON 파일로 다운로드합니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "JSON 파일", content = [Content(mediaType = "application/json")]),
    )
    @GetMapping("/documents/{documentId}/audit/export")
    fun exportDocumentAudit(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "문서 ID", required = true, example = "1")
        @PathVariable documentId: Long,
        response: HttpServletResponse,
    ) {
        signatureFlowService.getDetail(documentId, email)
        val logs = auditLogService.findByDocumentId(documentId).map { AuditLogResponse(it) }
        response.contentType = "application/json"
        response.setHeader("Content-Disposition", "attachment; filename=\"audit_document_${documentId}.json\"")
        response.writer.write(objectMapper.writeValueAsString(logs))
    }

    @Operation(
        summary = "번들 감사 로그 내보내기",
        description = "번들의 감사 로그를 JSON 파일로 다운로드합니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "JSON 파일", content = [Content(mediaType = "application/json")]),
    )
    @GetMapping("/bundles/{bundleId}/audit/export")
    fun exportBundleAudit(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "번들 ID", required = true, example = "1")
        @PathVariable bundleId: Long,
        response: HttpServletResponse,
    ) {
        signatureFlowService.getBundleDetail(bundleId, email)
        val logs = auditLogService.findByBundleId(bundleId).map { AuditLogResponse(it) }
        response.contentType = "application/json"
        response.setHeader("Content-Disposition", "attachment; filename=\"audit_bundle_${bundleId}.json\"")
        response.writer.write(objectMapper.writeValueAsString(logs))
    }
}
