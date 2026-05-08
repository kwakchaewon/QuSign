package com.qusign.common.email

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("prod")
class SesEmailService : EmailService {

    override fun sendSignatureRequest(
        to: String,
        token: String,
        documentName: String,
        requesterEmail: String,
        expiresAt: String,
    ) {
        // TODO: 4단계 — AWS SES 연동
        // - SesClient 주입
        // - HTML 템플릿 렌더링
        // - SendEmailRequest 구성 후 발송
        TODO("4단계: AWS SES 연동 구현 예정")
    }
}
