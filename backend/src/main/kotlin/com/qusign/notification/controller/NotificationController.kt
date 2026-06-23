package com.qusign.notification.controller

import com.qusign.auth.repository.UserRepository
import com.qusign.common.response.ApiResponse
import com.qusign.notification.dto.NotificationResponse
import com.qusign.notification.dto.SseTokenResponse
import com.qusign.notification.dto.UnreadCountResponse
import com.qusign.notification.service.NotificationService
import com.qusign.notification.service.SseEmitterRegistry
import com.qusign.notification.service.SseTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "Notification", description = "알림 목록 · 읽음 처리 · SSE 실시간 스트림")
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val registry: SseEmitterRegistry,
    private val userRepository: UserRepository,
    private val sseTokenService: SseTokenService,
) {

    @Operation(
        summary = "SSE 단기 토큰 발급",
        description = "EventSource는 Authorization 헤더를 지원하지 않으므로, 30초 유효한 일회성 SSE 토큰을 발급합니다. `/stream?token=...`에서 사용합니다."
    )
    @PostMapping("/sse-token")
    fun issueSseToken(authentication: Authentication): ApiResponse<SseTokenResponse> {
        val userId = resolveUserId(authentication)
        return ApiResponse.ok(SseTokenResponse(sseTokenService.issue(userId)))
    }

    @Operation(
        summary = "SSE 실시간 알림 스트림",
        description = "Server-Sent Events 스트림에 연결합니다. `POST /sse-token`으로 발급한 단기 토큰을 쿼리 파라미터로 전달해야 합니다."
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공 (text/event-stream)"),
        SwaggerApiResponse(responseCode = "401", description = "토큰 없음 또는 만료"),
    )
    @SecurityRequirements
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @Parameter(description = "POST /sse-token으로 발급한 단기 토큰", required = true)
        @RequestParam token: String?,
    ): SseEmitter {
        if (token.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 필요합니다.")
        }
        val userId = sseTokenService.consume(token)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다.")
        return registry.register(userId)
    }

    @Operation(summary = "알림 목록 조회", description = "페이지 단위로 알림 목록을 반환합니다. 최신순 정렬.")
    @GetMapping
    fun getNotifications(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        authentication: Authentication,
    ): ApiResponse<List<NotificationResponse>> {
        val userId = resolveUserId(authentication)
        return ApiResponse.ok(notificationService.getNotifications(userId, page))
    }

    @Operation(summary = "개별 알림 읽음 처리", description = "특정 알림 ID를 읽음 상태로 변경합니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "읽음 처리 성공"),
        SwaggerApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
    )
    @PutMapping("/{id}/read")
    fun markAsRead(
        @Parameter(description = "알림 ID", required = true, example = "1")
        @PathVariable id: Long,
        authentication: Authentication,
    ): ApiResponse<Unit> {
        val userId = resolveUserId(authentication)
        notificationService.markAsRead(id, userId)
        return ApiResponse.ok("읽음 처리되었습니다.")
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "로그인 사용자의 모든 미읽음 알림을 읽음 상태로 변경합니다.")
    @PutMapping("/read-all")
    fun markAllAsRead(authentication: Authentication): ApiResponse<Unit> {
        val userId = resolveUserId(authentication)
        notificationService.markAllAsRead(userId)
        return ApiResponse.ok("전체 읽음 처리되었습니다.")
    }

    @Operation(summary = "미읽음 알림 수 조회", description = "읽지 않은 알림의 개수를 반환합니다.")
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
