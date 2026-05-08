package com.qusign.common.email

interface EmailService {
    fun sendSignatureRequest(
        to: String,
        token: String,
        documentName: String,
        requesterEmail: String,
        expiresAt: String,
    )
}
