package com.qusign.notification.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.auth.repository.UserRepository
import com.qusign.notification.dto.NotificationResponse
import com.qusign.notification.entity.Notification
import com.qusign.notification.exception.NotificationNotFoundException
import com.qusign.notification.repository.NotificationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private const val CHANNEL_PREFIX = "notification:"
        private const val PAGE_SIZE = 50

        // 알림 타입 상수
        const val TYPE_SIGN_DONE = "SIGN_DONE"
        const val TYPE_SIGN_REQUEST = "SIGN_REQUEST"
        const val TYPE_SIGN_CANCELLED = "SIGN_CANCELLED"
        const val TYPE_SIGN_EXPIRING_SOON = "SIGN_EXPIRING_SOON"
        const val TYPE_SIGN_EXPIRED = "SIGN_EXPIRED"
    }

    @Transactional
    fun createAndPublish(
        userId: Long,
        type: String,
        title: String,
        message: String,
        referenceId: Long? = null,
        referenceToken: String? = null,
    ) {
        val user = userRepository.findById(userId).orElse(null) ?: return
        if (!isEnabled(user, type)) return
        val notification = notificationRepository.save(
            Notification(
                user = user,
                type = type,
                title = title,
                message = message,
                referenceId = referenceId,
                referenceToken = referenceToken,
            )
        )
        val payload = objectMapper.writeValueAsString(NotificationResponse(notification))
        redisTemplate.convertAndSend("$CHANNEL_PREFIX$userId", payload)
    }

    @Transactional(readOnly = true)
    fun getNotifications(userId: Long, page: Int = 0): List<NotificationResponse> =
        notificationRepository
            .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, PAGE_SIZE))
            .map { NotificationResponse(it) }

    @Transactional
    fun markAsRead(id: Long, userId: Long) {
        val n = notificationRepository.findByIdAndUserId(id, userId)
            ?: throw NotificationNotFoundException()
        n.isRead = true
    }

    @Transactional
    fun markAllAsRead(userId: Long) {
        notificationRepository.markAllAsReadByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getUnreadCount(userId: Long): Long =
        notificationRepository.countByUserIdAndIsRead(userId, false)

    private fun isEnabled(user: com.qusign.auth.entity.User, type: String): Boolean = when (type) {
        TYPE_SIGN_DONE, TYPE_SIGN_EXPIRED     -> user.notifySignDone
        TYPE_SIGN_REQUEST, TYPE_SIGN_CANCELLED,
        TYPE_SIGN_EXPIRING_SOON               -> user.notifySignRequest
        else                                  -> true
    }
}
