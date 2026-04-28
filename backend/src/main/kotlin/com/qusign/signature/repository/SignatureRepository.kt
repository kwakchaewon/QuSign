package com.qusign.signature.repository

import com.qusign.signature.entity.Signature
import com.qusign.signature.entity.SignatureRequest
import org.springframework.data.jpa.repository.JpaRepository

interface SignatureRepository : JpaRepository<Signature, Long> {
    fun findBySignatureRequest(signatureRequest: SignatureRequest): Signature?
}
