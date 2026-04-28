package com.qusign.document.repository

import com.qusign.auth.entity.User
import com.qusign.document.entity.Document
import org.springframework.data.jpa.repository.JpaRepository

interface DocumentRepository : JpaRepository<Document, Long> {
    fun findByUserOrderByCreatedAtDesc(user: User): List<Document>
    fun findByIdAndUser(id: Long, user: User): Document?
}
