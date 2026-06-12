package com.qusign.notification.controller

import com.qusign.auth.repository.UserRepository
import com.qusign.common.response.ApiResponse
import com.qusign.notification.dto.NotificationResponse
import com.qusign.notification.dto.SseTokenResponse
import com.qusign.notification.dto.UnreadCountResponse
import com.qusign.notification.service.NotificationService
import com.qusign.notification.service.SseEmitterRegistry
import com.qusign.notification.service.SseTokenService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val registry: SseEmitterRegistry,
    private val userRepository: UserRepository,
    private val sseTokenService: SseTokenService,
) {

    /** SSE 연결용 단기 토큰 발급 (30초 유효, 일회성) — JWT를 URL에 노출하지 않기 위해 사용 */
    @PostMapping("/sse-token")
    fun issueSseToken(authentication: Authentication): ApiResponse<SseTokenResponse> {
        val userId = resolveUserId(authentication)
        return ApiResponse.ok(SseTokenResponse(sseTokenService.issue(userId)))
    }

    /** EventSource는 커스텀 헤더 불가 → POST /sse-token으로 발급한 단기 토큰만 허용 */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(@RequestParam token: String?): SseEmitter {
        if (token.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 필요합니다.")
        }
        val userId = sseTokenService.consume(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다.")
        return registry.register(userId)
    }

    @GetMapping
    fun getNotifications(
        @RequestParam(defaultValue = "0") page: Int,
        authentication: Authentication,
    ): ApiResponse<List<NotificationResponse>> {
        val userId = resolveUserId(authentication)
        return ApiResponse.ok(notificationService.getNotifications(userId, page))
    }

    @PutMapping("/{id}/read")
    fun markAsRead(@PathVariable id: Long, authentication: Authentication): ApiResponse<Unit> {
        val userId = resolveUserId(authentication)
        notificationService.markAsRead(id, userId)
        return ApiResponse.ok("읽음 처리되었습니다.")
    }

    @PutMapping("/read-all")
    fun markAllAsRead(authentication: Authentication): ApiResponse<Unit> {
        val userId = resolveUserId(authentication)
        notificationService.markAllAsRead(userId)
        return ApiResponse.ok("전체 읽음 처리되었습니다.")
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(authentication: Authentication): ApiResponse<UnreadCountResponse> {
        val userId = resolveUserId(authentication)
        return ApiResponse.ok(UnreadCountResponse(notificationService.getUnreadCount(userId)))
    }

    private fun resolveUserId(authentication: Authentication): Long {
        val user = userRepository.findByEmail(authentication.name)
            ?: throw IllegalStateException("인증된 사용자를 찾을 수 없습니다.")
        return user.id
    }
}
