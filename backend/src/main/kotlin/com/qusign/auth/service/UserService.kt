package com.qusign.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.auth.dto.NotificationSettingsResponse
import com.qusign.auth.dto.UpdateNotificationSettingsRequest
import com.qusign.auth.dto.UserProfileResponse
import com.qusign.auth.exception.AccountDeletedException
import com.qusign.auth.exception.AccountDeletionNotAllowedException
import com.qusign.auth.exception.InvalidCurrentPasswordException
import com.qusign.auth.exception.PasswordChangeNotAllowedException
import com.qusign.auth.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val keyEncryptionService: KeyEncryptionService,
    private val passwordEncoder: PasswordEncoder,
    private val objectMapper: ObjectMapper,
    @Value("\${admin.email:}") private val adminEmail: String,
) {
    companion object {
        private val PROTECTED_EMAILS = setOf("signer@test.com", "requestor@test.com")
    }

    fun getProfile(email: String): UserProfileResponse {
        val user = userRepository.findByEmail(email) ?: throw NoSuchElementException("사용자를 찾을 수 없습니다")
        if (user.deletedAt != null) throw AccountDeletedException()
        return UserProfileResponse(
            email = user.email,
            notifySignRequest = user.notifySignRequest,
            notifySignDone = user.notifySignDone,
            notifyWeekly = user.notifyWeekly,
            notifyMarketing = user.notifyMarketing,
        )
    }

    @Transactional
    fun changePassword(email: String, currentPassword: String, newPassword: String) {
        if (email in PROTECTED_EMAILS || (adminEmail.isNotBlank() && email == adminEmail)) {
            throw PasswordChangeNotAllowedException()
        }
        val user = userRepository.findByEmail(email) ?: throw NoSuchElementException("사용자를 찾을 수 없습니다")
        if (user.deletedAt != null) throw AccountDeletedException()
        if (!passwordEncoder.matches(currentPassword, user.password)) throw InvalidCurrentPasswordException()

        val encryptedKey = objectMapper.readValue(user.encryptedPrivateKey, EncryptedKey::class.java)
        val privateKeyBytes = keyEncryptionService.decrypt(encryptedKey, currentPassword)
        val reEncryptedKey = keyEncryptionService.encrypt(privateKeyBytes, newPassword)

        user.password = passwordEncoder.encode(newPassword)
        user.encryptedPrivateKey = objectMapper.writeValueAsString(reEncryptedKey)
    }

    @Transactional
    fun updateNotificationSettings(email: String, request: UpdateNotificationSettingsRequest): NotificationSettingsResponse {
        val user = userRepository.findByEmail(email) ?: throw NoSuchElementException("사용자를 찾을 수 없습니다")
        if (user.deletedAt != null) throw AccountDeletedException()

        user.notifySignRequest = request.notifySignRequest
        user.notifySignDone = request.notifySignDone
        user.notifyWeekly = request.notifyWeekly
        user.notifyMarketing = request.notifyMarketing

        return NotificationSettingsResponse(
            notifySignRequest = user.notifySignRequest,
            notifySignDone = user.notifySignDone,
            notifyWeekly = user.notifyWeekly,
            notifyMarketing = user.notifyMarketing,
        )
    }

    fun searchUsers(requesterEmail: String, q: String): List<String> {
        if (q.length < 2) return emptyList()
        return userRepository.findTop8ByEmailContainingIgnoreCaseAndDeletedAtIsNullOrderByEmail(q)
            .filter { it.role != "ADMIN" && it.email != requesterEmail }
            .map { it.email }
    }

    @Transactional
    fun deleteAccount(email: String) {
        if (email in PROTECTED_EMAILS) throw AccountDeletionNotAllowedException()
        val user = userRepository.findByEmail(email) ?: throw NoSuchElementException("사용자를 찾을 수 없습니다")
        if (user.deletedAt != null) throw AccountDeletedException()
        user.deletedAt = LocalDateTime.now()
    }
}
