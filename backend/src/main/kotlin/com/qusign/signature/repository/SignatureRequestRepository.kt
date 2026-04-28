package com.qusign.signature.repository

import com.qusign.signature.entity.SignatureRequest
import org.springframework.data.jpa.repository.JpaRepository

interface SignatureRequestRepository : JpaRepository<SignatureRequest, Long> {
    fun findByToken(token: String): SignatureRequest?
}
