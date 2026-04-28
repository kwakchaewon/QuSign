package com.qusign.signature.entity

import com.qusign.auth.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "signatures")
class Signature(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_request_id", nullable = false)
    val signatureRequest: SignatureRequest,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signer_id", nullable = false)
    val signer: User,

    @Column(name = "signed_storage_key", nullable = false)
    val signedStorageKey: String,

    @Column(name = "signature_value", nullable = false, columnDefinition = "TEXT")
    val signatureValue: String,

    @Column(name = "signed_at", insertable = false, updatable = false)
    val signedAt: LocalDateTime? = null,
)
