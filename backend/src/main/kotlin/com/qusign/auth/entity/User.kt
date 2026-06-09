package com.qusign.auth.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Column(name = "encrypted_private_key", nullable = false, columnDefinition = "TEXT")
    var encryptedPrivateKey: String,

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    val publicKey: String,

    @Column(name = "notify_sign_request")
    var notifySignRequest: Boolean = true,

    @Column(name = "notify_sign_done")
    var notifySignDone: Boolean = true,

    @Column(name = "notify_weekly")
    var notifyWeekly: Boolean = false,

    @Column(name = "notify_marketing")
    var notifyMarketing: Boolean = false,

    @Column(nullable = false)
    var role: String = "USER",

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @Column(name = "disabled_at")
    var disabledAt: LocalDateTime? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @Column(name = "updated_at", insertable = false, updatable = false)
    val updatedAt: LocalDateTime? = null,
)
