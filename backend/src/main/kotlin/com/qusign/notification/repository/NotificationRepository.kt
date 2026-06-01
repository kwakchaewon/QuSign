package com.qusign.notification.repository

import com.qusign.notification.entity.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface NotificationRepository : JpaRepository<Notification, Long> {

    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): List<Notification>

    fun countByUserIdAndIsRead(userId: Long, isRead: Boolean): Long

    fun findByIdAndUserId(id: Long, userId: Long): Notification?

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    fun markAllAsReadByUserId(userId: Long)
}
