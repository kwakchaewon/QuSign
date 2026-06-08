package com.qusign.document.entity

import jakarta.persistence.*

@Entity
@Table(name = "document_bundle_items")
class DocumentBundleItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    val bundle: DocumentBundle,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    val document: Document,

    @Column(nullable = false)
    val ordinal: Int = 0,
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
