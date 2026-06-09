package com.qusign.audit.dto

import com.qusign.audit.entity.AuditEventType
import com.qusign.audit.entity.AuditLog

data class AuditLogResponse(
    val id: Long,
    val eventType: AuditEventType,
    val actorEmail: String,
    val ipAddress: String,
    val createdAt: String,
    val signatureRequestId: Long?,
    val bundleId: Long?,
    val documentId: Long?,
) {
    constructor(log: AuditLog) : this(
        id = log.id,
        eventType = log.eventType,
        actorEmail = log.actorEmail,
        ipAddress = log.ipAddress,
        createdAt = log.createdAt.toString(),
        signatureRequestId = log.signatureRequestId,
        bundleId = log.bundleId,
        documentId = log.documentId,
    )
}
