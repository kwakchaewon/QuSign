package com.qusign.audit.service

import com.qusign.audit.RetentionPolicy
import com.qusign.audit.dto.AuditLogResponse
import com.qusign.audit.entity.AuditEventType
import com.qusign.audit.entity.AuditLog
import com.qusign.audit.repository.AuditLogRepository
import com.qusign.common.audit.AuditContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class AuditLogService(private val auditLogRepository: AuditLogRepository) {

    // REQUIRES_NEW: 호출자 트랜잭션(read-only 포함)과 무관하게 별도 쓰기 트랜잭션으로 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun record(
        eventType: AuditEventType,
        actorEmail: String,
        auditCtx: AuditContext,
        signatureRequestId: Long? = null,
        bundleId: Long? = null,
        documentId: Long? = null,
    ) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        auditLogRepository.save(
            AuditLog(
                eventType = eventType,
                actorEmail = actorEmail,
                signatureRequestId = signatureRequestId,
                bundleId = bundleId,
                documentId = documentId,
                ipAddress = auditCtx.ipAddress.take(45),
                userAgent = auditCtx.userAgent.take(500),
                createdAt = now,
                retainedUntil = now.plus(RetentionPolicy.AUDIT_LOG),
            )
        )
    }

    @Transactional(readOnly = true)
    fun findByDocumentId(documentId: Long): List<AuditLog> =
        auditLogRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)

    @Transactional(readOnly = true)
    fun findByBundleId(bundleId: Long): List<AuditLog> =
        auditLogRepository.findByBundleIdOrderByCreatedAtAsc(bundleId)

    // Admin: 전체 감사 로그 (필터 + 페이징)
    @Transactional(readOnly = true)
    fun findAll(
        page: Int,
        size: Int,
        eventType: AuditEventType?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
    ): Page<AuditLogResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return auditLogRepository.findAllWithFilters(eventType, startDate, endDate, pageable)
            .map { AuditLogResponse(it) }
    }

    // Admin: 특정 사용자의 감사 로그 (페이징)
    @Transactional(readOnly = true)
    fun findByActorEmail(email: String, page: Int, size: Int): Page<AuditLogResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        return auditLogRepository.findByActorEmailOrderByCreatedAtDesc(email, pageable)
            .map { AuditLogResponse(it) }
    }

    // Admin: 전체 감사 로그 내보내기 (JSON)
    @Transactional(readOnly = true)
    fun exportAll(): List<AuditLogResponse> =
        auditLogRepository.findAllByOrderByCreatedAtAsc().map { AuditLogResponse(it) }
}
