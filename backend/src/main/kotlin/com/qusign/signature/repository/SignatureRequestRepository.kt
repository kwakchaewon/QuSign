package com.qusign.signature.repository

import com.qusign.document.entity.Document
import com.qusign.signature.entity.SignatureRequest
import org.springframework.data.jpa.repository.JpaRepository

interface SignatureRequestRepository : JpaRepository<SignatureRequest, Long> {
    fun findByToken(token: String): SignatureRequest?
    fun findByDocumentOrderByCreatedAtAsc(document: Document): List<SignatureRequest>
    fun findByDocumentAndSignerEmail(document: Document, signerEmail: String): SignatureRequest?
}
