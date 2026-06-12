package com.qusign.notification.dto

import com.qusign.notification.entity.Notification

data class NotificationResponse(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val referenceId: Long?,
    val referenceToken: String?,
    val isRead: Boolean,
    val createdAt: String,
) {
    constructor(n: Notification) : this(
        id = n.id,
        type = n.type,
        title = n.title,
        message = n.message,
        referenceId = n.referenceId,
        referenceToken = n.referenceToken,
        isRead = n.isRead,
        createdAt = n.createdAt?.toString() ?: "",
    )
}

data class UnreadCountResponse(val count: Long)

data class SseTokenResponse(val sseToken: String)
