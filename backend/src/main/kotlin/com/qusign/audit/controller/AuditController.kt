package com.qusign.audit.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.audit.dto.AuditLogResponse
import com.qusign.audit.service.AuditLogService
import com.qusign.common.response.ApiResponse
import com.qusign.signature.service.SignatureFlowService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class AuditController(
    private val auditLogService: AuditLogService,
    private val signatureFlowService: SignatureFlowService,
    private val objectMapper: ObjectMapper,
) {

    @GetMapping("/documents/{documentId}/audit")
    fun getDocumentAudit(
        @AuthenticationPrincipal email: String,
        @PathVariable documentId: Long,
    ): ApiResponse<List<AuditLogResponse>> {
        signatureFlowService.getDetail(documentId, email) // 소유자 권한 확인
        return ApiResponse.ok(auditLogService.findByDocumentId(documentId).map { AuditLogResponse(it) })
    }

    @GetMapping("/bundles/{bundleId}/audit")
    fun getBundleAudit(
        @AuthenticationPrincipal email: String,
        @PathVariable bundleId: Long,
    ): ApiResponse<List<AuditLogResponse>> {
        signatureFlowService.getBundleDetail(bundleId, email) // 소유자 권한 확인
        return ApiResponse.ok(auditLogService.findByBundleId(bundleId).map { AuditLogResponse(it) })
    }

    @GetMapping("/documents/{documentId}/audit/export")
    fun exportDocumentAudit(
        @AuthenticationPrincipal email: String,
        @PathVariable documentId: Long,
        response: HttpServletResponse,
    ) {
        signatureFlowService.getDetail(documentId, email) // 소유자 권한 확인
        val logs = auditLogService.findByDocumentId(documentId).map { AuditLogResponse(it) }
        response.contentType = "application/json"
        response.setHeader("Content-Disposition", "attachment; filename=\"audit_document_${documentId}.json\"")
        response.writer.write(objectMapper.writeValueAsString(logs))
    }

    @GetMapping("/bundles/{bundleId}/audit/export")
    fun exportBundleAudit(
        @AuthenticationPrincipal email: String,
        @PathVariable bundleId: Long,
        response: HttpServletResponse,
    ) {
        signatureFlowService.getBundleDetail(bundleId, email) // 소유자 권한 확인
        val logs = auditLogService.findByBundleId(bundleId).map { AuditLogResponse(it) }
        response.contentType = "application/json"
        response.setHeader("Content-Disposition", "attachment; filename=\"audit_bundle_${bundleId}.json\"")
        response.writer.write(objectMapper.writeValueAsString(logs))
    }
}
