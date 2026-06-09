package com.qusign.audit.service

import com.qusign.audit.entity.AuditEventType
import com.qusign.audit.entity.AuditLog
import com.qusign.audit.repository.AuditLogRepository
import com.qusign.common.audit.AuditContext
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
        auditLogRepository.save(
            AuditLog(
                eventType = eventType,
                actorEmail = actorEmail,
                signatureRequestId = signatureRequestId,
                bundleId = bundleId,
                documentId = documentId,
                ipAddress = auditCtx.ipAddress.take(45),
                userAgent = auditCtx.userAgent.take(500),
                createdAt = LocalDateTime.now(ZoneOffset.UTC),
            )
        )
    }

    @Transactional(readOnly = true)
    fun findByDocumentId(documentId: Long): List<AuditLog> =
        auditLogRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)

    @Transactional(readOnly = true)
    fun findByBundleId(bundleId: Long): List<AuditLog> =
        auditLogRepository.findByBundleIdOrderByCreatedAtAsc(bundleId)
}
