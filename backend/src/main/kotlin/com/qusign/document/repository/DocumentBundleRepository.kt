package com.qusign.document.repository

import com.qusign.auth.entity.User
import com.qusign.document.entity.Document
import com.qusign.document.entity.DocumentBundle
import com.qusign.document.entity.DocumentBundleItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface DocumentBundleRepository : JpaRepository<DocumentBundle, Long> {
    fun findByIdAndOwner(id: Long, owner: User): DocumentBundle?

    @Query("SELECT b FROM DocumentBundle b WHERE b.owner = :owner ORDER BY b.createdAt DESC")
    fun findByOwnerOrderByCreatedAtDesc(owner: User): List<DocumentBundle>
}

interface DocumentBundleItemRepository : JpaRepository<DocumentBundleItem, Long> {
    fun findByDocumentIn(documents: List<Document>): List<DocumentBundleItem>
    fun findByBundle(bundle: DocumentBundle): List<DocumentBundleItem>
}
