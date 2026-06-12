package com.qusign.common.audit

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AuditContextExtractor(
    @Value("\${proxy.trusted:false}") private val trustedProxy: Boolean,
) {
    fun from(request: HttpServletRequest): AuditContext {
        val ip = if (trustedProxy) {
            // 신뢰된 리버스 프록시(nginx/ALB) 뒤에 있을 때만 X-Forwarded-For 헤더 신뢰
            (request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
                ?: request.remoteAddr).take(45)
        } else {
            // 기본: TCP 연결 주소만 사용 → 헤더 스푸핑 불가
            request.remoteAddr.take(45)
        }
        val ua = (request.getHeader("User-Agent") ?: "").take(500)
        return AuditContext(ipAddress = ip, userAgent = ua)
    }
}
