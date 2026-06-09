package com.qusign.audit.repository

import com.qusign.audit.entity.AuditLog
import org.springframework.data.jpa.repository.JpaRepository

interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    fun findByDocumentIdOrderByCreatedAtAsc(documentId: Long): List<AuditLog>
    fun findByBundleIdOrderByCreatedAtAsc(bundleId: Long): List<AuditLog>
}
