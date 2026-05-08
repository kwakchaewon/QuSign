package com.qusign.common.email

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("local")
class ConsoleEmailService(
    @Value("\${email.base-url}") private val baseUrl: String,
) : EmailService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendSignatureRequest(
        to: String,
        token: String,
        documentName: String,
        requesterEmail: String,
        expiresAt: String,
    ) {
        log.info(
            """
            ┌─────────────────────────────────────────────────────┐
            │  [EMAIL] 서명 요청 — 로컬 환경 (실제 발송 안 됨)        │
            ├─────────────────────────────────────────────────────┤
            │  수신자  : $to
            │  요청자  : $requesterEmail
            │  문서명  : $documentName
            │  만료일  : $expiresAt
            │  서명링크: $baseUrl/sign/$token
            └─────────────────────────────────────────────────────┘
            """.trimIndent()
        )
    }
}
