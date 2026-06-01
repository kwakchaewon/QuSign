package com.qusign.notification.controller

import com.qusign.auth.repository.UserRepository
import com.qusign.common.response.ApiResponse
import com.qusign.notification.dto.NotificationResponse
import com.qusign.notification.dto.UnreadCountResponse
import com.qusign.notification.service.NotificationService
import com.qusign.notification.service.SseEmitterRegistry
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val registry: SseEmitterRegistry,
    private val userRepository: UserRepository,
) {

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(authentication: Authentication): SseEmitter {
        val userId = resolveUserId(authentication)
        return registry.register(userId)
    }

    @GetMapping
    fun getNotifications(authentication: Authentication): ApiResponse<List<NotificationResponse>> {
        val userId = resolveUserId(authentication)
        return ApiResponse.ok(notificationService.getNotifications(userId))
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
