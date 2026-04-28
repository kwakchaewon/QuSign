package com.qusign.signature.entity

import com.qusign.auth.entity.User
import com.qusign.document.entity.Document
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "signature_requests")
class SignatureRequest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    val document: Document,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    val requester: User,

    @Column(name = "signer_email", nullable = false)
    val signerEmail: String,

    @Column(nullable = false, length = 36)
    val token: String,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false, length = 20)
    var status: String = "PENDING",

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,
)
