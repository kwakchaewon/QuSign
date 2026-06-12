package com.qusign.notification.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

private const val SSE_TOKEN_PREFIX = "sse-token:"
private val SSE_TOKEN_TTL = Duration.ofSeconds(30)

@Service
class SseTokenService(private val redisTemplate: RedisTemplate<String, String>) {

    /** 인증된 사용자 ID에 대한 30초 유효 SSE 단기 토큰 발급 */
    fun issue(userId: Long): String {
        val token = UUID.randomUUID().toString()
        redisTemplate.opsForValue().set("$SSE_TOKEN_PREFIX$token", userId.toString(), SSE_TOKEN_TTL)
        return token
    }

    /** 토큰으로 userId 조회 후 즉시 삭제 (일회성) */
    fun consume(token: String): Long? {
        val key = "$SSE_TOKEN_PREFIX$token"
        val value = redisTemplate.opsForValue().getAndDelete(key) ?: return null
        return value.toLongOrNull()
    }
}
