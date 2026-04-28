package com.qusign.document.entity

import com.qusign.auth.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "documents")
class Document(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "original_filename", nullable = false)
    val originalFilename: String,

    @Column(name = "storage_key", nullable = false)
    val storageKey: String,

    @Column(name = "hash_sha3_256", nullable = false, length = 64)
    val hashSha3256: String,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,
)
