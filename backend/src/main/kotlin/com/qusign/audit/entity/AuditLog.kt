package com.qusign.audit.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    val eventType: AuditEventType,

    @Column(name = "actor_email", nullable = false)
    val actorEmail: String,

    @Column(name = "signature_request_id")
    val signatureRequestId: Long? = null,

    @Column(name = "bundle_id")
    val bundleId: Long? = null,

    @Column(name = "document_id")
    val documentId: Long? = null,

    @Column(name = "ip_address", nullable = false, length = 45)
    val ipAddress: String,

    @Column(name = "user_agent", nullable = false, length = 500)
    val userAgent: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,

    // 전자서명법 제31조: 보존 기한 — createdAt + 10년, 삽입 후 변경 금지
    @Column(name = "retained_until", nullable = false, updatable = false)
    val retainedUntil: LocalDateTime,
)
