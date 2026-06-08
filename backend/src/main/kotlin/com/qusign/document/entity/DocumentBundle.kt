package com.qusign.document.entity

import com.qusign.auth.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "document_bundles")
class DocumentBundle(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,

    @OneToMany(mappedBy = "bundle", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @OrderBy("ordinal ASC")
    val items: MutableList<DocumentBundleItem> = mutableListOf(),

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,
)
