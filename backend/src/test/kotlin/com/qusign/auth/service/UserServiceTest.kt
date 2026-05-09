package com.qusign.auth.service

import com.qusign.auth.dto.UpdateNotificationSettingsRequest
import com.qusign.auth.exception.AccountDeletedException
import com.qusign.auth.exception.InvalidCurrentPasswordException
import com.qusign.auth.repository.UserRepository
import com.qusign.common.email.EmailService
import com.qusign.common.storage.StorageService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@Transactional
class UserServiceTest {

    @MockitoBean lateinit var storageService: StorageService
    @MockitoBean lateinit var emailService: EmailService

    @Autowired lateinit var authService: AuthService
    @Autowired lateinit var userService: UserService
    @Autowired lateinit var userRepository: UserRepository

    @Test
    fun `프로필 조회 성공`() {
        authService.register("profile@qusign.com", "password123")
        val profile = userService.getProfile("profile@qusign.com")
        assertEquals("profile@qusign.com", profile.email)
    }

    @Test
    fun `비밀번호 변경 성공 후 새 비밀번호로 로그인 가능`() {
        authService.register("pwchange@qusign.com", "oldPass123!")
        userService.changePassword("pwchange@qusign.com", "oldPass123!", "newPass456!")

        val token = authService.login("pwchange@qusign.com", "newPass456!")
        assertNotNull(token)
    }

    @Test
    fun `잘못된 현재 비밀번호로 변경 시 예외 발생`() {
        authService.register("badpw@qusign.com", "correctPass1!")
        assertThrows<InvalidCurrentPasswordException> {
            userService.changePassword("badpw@qusign.com", "wrongPass!", "newPass456!")
        }
    }

    @Test
    fun `비밀번호 변경 후 기존 비밀번호로 로그인 불가`() {
        authService.register("lockout@qusign.com", "oldPass123!")
        userService.changePassword("lockout@qusign.com", "oldPass123!", "newPass456!")

        assertThrows<InvalidCurrentPasswordException> {
            userService.changePassword("lockout@qusign.com", "oldPass123!", "anotherPass7!")
        }
    }

    @Test
    fun `알림 설정 업데이트 및 조회`() {
        authService.register("notify@qusign.com", "password123")

        val request = UpdateNotificationSettingsRequest(
            notifySignRequest = false,
            notifySignDone = true,
            notifyWeekly = true,
            notifyMarketing = false,
        )
        val result = userService.updateNotificationSettings("notify@qusign.com", request)

        assertEquals(false, result.notifySignRequest)
        assertEquals(true, result.notifyWeekly)

        val profile = userService.getProfile("notify@qusign.com")
        assertEquals(false, profile.notifySignRequest)
        assertEquals(true, profile.notifyWeekly)
    }

    @Test
    fun `계정 삭제 후 soft delete 확인`() {
        authService.register("delete@qusign.com", "password123")
        userService.deleteAccount("delete@qusign.com")

        val user = userRepository.findByEmail("delete@qusign.com")
        assertNotNull(user?.deletedAt)
    }

    @Test
    fun `탈퇴한 계정으로 로그인 시 예외 발생`() {
        authService.register("gone@qusign.com", "password123")
        userService.deleteAccount("gone@qusign.com")

        assertThrows<AccountDeletedException> {
            authService.login("gone@qusign.com", "password123")
        }
    }

    @Test
    fun `탈퇴한 계정 프로필 조회 시 예외 발생`() {
        authService.register("gone2@qusign.com", "password123")
        userService.deleteAccount("gone2@qusign.com")

        assertThrows<AccountDeletedException> {
            userService.getProfile("gone2@qusign.com")
        }
    }
}
