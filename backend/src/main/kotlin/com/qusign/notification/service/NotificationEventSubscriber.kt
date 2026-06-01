package com.qusign.notification.service

import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

@Component
class NotificationEventSubscriber(
    private val registry: SseEmitterRegistry,
) : MessageListener {

    override fun onMessage(message: Message, pattern: ByteArray?) {
        // 채널 형식: "notification:<userId>"
        val channel = String(message.channel)
        val userId = channel.substringAfterLast(":").toLongOrNull() ?: return
        val payload = String(message.body)
        registry.send(userId, payload)
    }
}
