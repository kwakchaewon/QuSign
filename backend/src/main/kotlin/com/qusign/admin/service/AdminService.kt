package com.qusign.admin.service

import com.qusign.admin.dto.AdminStatsResponse
import com.qusign.admin.dto.AdminUserResponse
import com.qusign.admin.dto.PagedResponse
import com.qusign.audit.dto.AuditLogResponse
import com.qusign.audit.entity.AuditEventType
import com.qusign.audit.repository.AuditLogRepository
import com.qusign.auth.repository.UserRepository
import com.qusign.signature.repository.SignatureRequestRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val auditLogRepository: AuditLogRepository,
    private val signatureRequestRepository: SignatureRequestRepository,
) {

    @Transactional(readOnly = true)
    fun getStats(): AdminStatsResponse {
        val totalUsers = userRepository.countByDeletedAtIsNull()
        val totalSignatures = signatureRequestRepository.count()
        val pending = signatureRequestRepository.countByStatus("PENDING")
        val completed = signatureRequestRepository.countByStatus("SIGNED")
        val cancelled = signatureRequestRepository.countByStatus("CANCELLED")
        return AdminStatsResponse(
            totalUsers = totalUsers,
            totalSignatures = totalSignatures,
            pendingSignatures = pending,
            completedSignatures = completed,
            cancelledSignatures = cancelled,
        )
    }

    @Transactional(readOnly = true)
    fun getUsers(page: Int, size: Int, email: String?): PagedResponse<AdminUserResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("email").ascending())
        val result = if (email.isNullOrBlank())
            userRepository.findAllByDeletedAtIsNull(pageable)
        else
            userRepository.findByEmailContainingIgnoreCaseAndDeletedAtIsNull(email, pageable)
        return PagedResponse(
            content = result.content.map { AdminUserResponse(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @Transactional
    fun disableUser(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw NoSuchElementException("사용자를 찾을 수 없습니다: $email")
        if (user.deletedAt != null) throw NoSuchElementException("이미 탈퇴한 사용자입니다: $email")
        if (user.disabledAt != null) return // 이미 비활성화 — 멱등 처리
        user.disabledAt = LocalDateTime.now()
    }

    @Transactional(readOnly = true)
    fun getAuditLogs(
        page: Int,
        size: Int,
        eventType: AuditEventType?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
    ): PagedResponse<AuditLogResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = auditLogRepository.findAllWithFilters(eventType, startDate, endDate, pageable)
        return PagedResponse(
            content = result.content.map { AuditLogResponse(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @Transactional(readOnly = true)
    fun getUserAuditLogs(email: String, page: Int, size: Int): PagedResponse<AuditLogResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = auditLogRepository.findByActorEmailOrderByCreatedAtDesc(email, pageable)
        return PagedResponse(
            content = result.content.map { AuditLogResponse(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @Transactional(readOnly = true)
    fun exportAuditLogs(): List<AuditLogResponse> =
        auditLogRepository.findAllByOrderByCreatedAtAsc().map { AuditLogResponse(it) }
}
