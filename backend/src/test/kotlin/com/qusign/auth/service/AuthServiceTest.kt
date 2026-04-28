package com.qusign.auth.service

import com.qusign.auth.exception.EmailAlreadyExistsException
import com.qusign.auth.exception.InvalidCredentialsException
import com.qusign.auth.repository.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired lateinit var authService: AuthService
    @Autowired lateinit var jwtService: JwtService
    @Autowired lateinit var userRepository: UserRepository

    @Test
    fun `회원가입 성공 후 DB 저장 확인`() {
        authService.register("test@qusign.com", "password123")

        val user = userRepository.findByEmail("test@qusign.com")
        assertNotNull(user)
        assertTrue(user.encryptedPrivateKey.isNotBlank())
        assertTrue(user.publicKey.isNotBlank())
    }

    @Test
    fun `중복 이메일 회원가입 시 예외 발생`() {
        authService.register("dup@qusign.com", "password123")
        assertThrows<EmailAlreadyExistsException> {
            authService.register("dup@qusign.com", "password123")
        }
    }

    @Test
    fun `로그인 성공 시 JWT 토큰 반환`() {
        authService.register("login@qusign.com", "password123")
        val token = authService.login("login@qusign.com", "password123")

        assertNotNull(token)
        assertTrue(jwtService.isValid(token))
        assertTrue(jwtService.extractEmail(token) == "login@qusign.com")
    }

    @Test
    fun `존재하지 않는 이메일 로그인 시 예외 발생`() {
        assertThrows<InvalidCredentialsException> {
            authService.login("nobody@qusign.com", "password123")
        }
    }

    @Test
    fun `잘못된 비밀번호 로그인 시 예외 발생`() {
        authService.register("wrong@qusign.com", "password123")
        assertThrows<InvalidCredentialsException> {
            authService.login("wrong@qusign.com", "wrongpassword")
        }
    }
}
