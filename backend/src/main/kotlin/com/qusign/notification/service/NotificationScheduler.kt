package com.qusign.notification.service

import com.qusign.auth.repository.UserRepository
import com.qusign.signature.repository.SignatureRequestRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class NotificationScheduler(
    private val signatureRequestRepository: SignatureRequestRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
) {

    // 매일 오전 9시: 만료 D-1 알림 (서명자에게)
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    fun sendExpiringSoonNotifications() {
        val now = LocalDateTime.now()
        val expiring = signatureRequestRepository.findExpiringBetween(
            from = now,
            to = now.plusHours(24),
        )
        expiring.forEach { req ->
            val signer = userRepository.findByEmail(req.signerEmail) ?: return@forEach
            notificationService.createAndPublish(
                userId = signer.id,
                type = NotificationService.TYPE_SIGN_EXPIRING_SOON,
                title = "서명 기한 임박",
                message = "'${req.document.originalFilename}' 서명 기한이 24시간 남았습니다.",
                referenceId = req.id,
            )
        }
    }

    // 매일 오전 9시 1분: 만료 알림 (요청자에게)
    @Scheduled(cron = "0 1 9 * * *")
    @Transactional(readOnly = true)
    fun sendExpiredNotifications() {
        val expired = signatureRequestRepository.findExpiredPending(LocalDateTime.now())
        expired.forEach { req ->
            val requester = req.requester
            notificationService.createAndPublish(
                userId = requester.id,
                type = NotificationService.TYPE_SIGN_EXPIRED,
                title = "서명 기한 만료",
                message = "'${req.document.originalFilename}'의 ${req.signerEmail} 서명 기한이 만료되었습니다.",
                referenceId = req.document.id,
            )
        }
    }
}
