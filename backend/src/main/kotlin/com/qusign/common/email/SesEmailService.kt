package com.qusign.common.email

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

// [SES보류] SES 연동 구현 시 아래 sendSignatureRequest 본문을 채운다.
@Service
@Profile("prod")
class SesEmailService : EmailService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendSignatureRequest(
        to: String,
        token: String,
        documentName: String,
        requesterEmail: String,
        expiresAt: String,
    ) {
        // [SES보류] SesClient 주입 → HTML 템플릿 렌더링 → SendEmailRequest 발송
        log.warn("[SES미연동] 이메일 발송 생략 — 수신자: {}", to)
    }
}
