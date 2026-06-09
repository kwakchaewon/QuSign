package com.qusign.signature.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "timestamp_tokens")
class TimestampToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signature_id", nullable = false, unique = true)
    val signature: Signature,

    @Column(name = "tsa_url", nullable = false, length = 500)
    val tsaUrl: String,

    @Column(name = "serial_number", nullable = false, length = 100)
    val serialNumber: String,

    /** TSA 공인 시각 (UTC) */
    @Column(name = "gen_time", nullable = false)
    val genTime: LocalDateTime,

    @Column(name = "hash_algorithm", nullable = false, length = 20)
    val hashAlgorithm: String = "SHA-256",

    /** TSA가 서명한 콘텐츠(서명된 PDF)의 SHA-256 해시 */
    @Column(name = "message_imprint", nullable = false, columnDefinition = "VARBINARY(64)")
    val messageImprint: ByteArray,

    /** DER 인코딩된 RFC 3161 TimeStampToken */
    @Column(name = "token_der", nullable = false, columnDefinition = "LONGBLOB")
    val tokenDer: ByteArray,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,
)
