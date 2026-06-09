package com.qusign.audit.repository

import com.qusign.audit.entity.AuditEventType
import com.qusign.audit.entity.AuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface AuditLogRepository : JpaRepository<AuditLog, Long> {
    fun findByDocumentIdOrderByCreatedAtAsc(documentId: Long): List<AuditLog>
    fun findByBundleIdOrderByCreatedAtAsc(bundleId: Long): List<AuditLog>

    // Admin: 전체 감사 로그 (이벤트 타입 + 날짜 범위 필터, 페이징)
    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:eventType IS NULL OR a.eventType = :eventType)
        AND (:startDate IS NULL OR a.createdAt >= :startDate)
        AND (:endDate IS NULL OR a.createdAt <= :endDate)
        ORDER BY a.createdAt DESC
    """)
    fun findAllWithFilters(
        @Param("eventType") eventType: AuditEventType?,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
        pageable: Pageable,
    ): Page<AuditLog>

    // Admin: 특정 사용자의 감사 로그 (페이징)
    fun findByActorEmailOrderByCreatedAtDesc(actorEmail: String, pageable: Pageable): Page<AuditLog>

    // Admin: 전체 감사 로그 JSON 내보내기 (법적 분쟁·감사 대응)
    fun findAllByOrderByCreatedAtAsc(): List<AuditLog>
}
