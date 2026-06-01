package com.qusign.signature.repository

import com.qusign.document.entity.Document
import com.qusign.signature.entity.SignatureRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface SignatureRequestRepository : JpaRepository<SignatureRequest, Long> {
    fun findByToken(token: String): SignatureRequest?
    fun findByDocumentOrderByCreatedAtAsc(document: Document): List<SignatureRequest>
    fun findByDocumentAndSignerEmail(document: Document, signerEmail: String): SignatureRequest?
    fun findByDocumentIn(documents: List<Document>): List<SignatureRequest>

    // 만료 D-1 (24시간 이내 만료 예정 PENDING 요청)
    @Query("SELECT r FROM SignatureRequest r WHERE r.status = 'PENDING' AND r.expiresAt BETWEEN :from AND :to")
    fun findExpiringBetween(from: LocalDateTime, to: LocalDateTime): List<SignatureRequest>

    // 이미 만료된 PENDING 요청 (알림 미발송분 처리용)
    @Query("SELECT r FROM SignatureRequest r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    fun findExpiredPending(now: LocalDateTime): List<SignatureRequest>
}
