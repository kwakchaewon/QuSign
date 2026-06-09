package com.qusign.signature.repository

import com.qusign.signature.entity.Signature
import com.qusign.signature.entity.TimestampToken
import org.springframework.data.jpa.repository.JpaRepository

interface TimestampTokenRepository : JpaRepository<TimestampToken, Long> {
    fun findBySignature(signature: Signature): TimestampToken?
}
