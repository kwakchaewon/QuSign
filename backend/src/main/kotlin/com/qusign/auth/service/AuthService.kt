package com.qusign.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.auth.entity.User
import com.qusign.auth.exception.AccountDeletedException
import com.qusign.auth.exception.AccountDisabledException
import com.qusign.auth.exception.EmailAlreadyExistsException
import com.qusign.auth.exception.InvalidCredentialsException
import com.qusign.auth.repository.UserRepository
import com.qusign.signature.service.PqcSignatureService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val pqcSignatureService: PqcSignatureService,
    private val keyEncryptionService: KeyEncryptionService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun register(email: String, password: String) {
        if (userRepository.existsByEmail(email)) throw EmailAlreadyExistsException(email)

        val keyPair = pqcSignatureService.generateKeyPair()
        val encryptedKey = keyEncryptionService.encrypt(keyPair.private.encoded, password)

        userRepository.save(
            User(
                email = email,
                password = passwordEncoder.encode(password),
                encryptedPrivateKey = objectMapper.writeValueAsString(encryptedKey),
                publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded),
            )
        )
    }

    fun login(email: String, password: String): String {
        val user = userRepository.findByEmail(email) ?: throw InvalidCredentialsException()
        if (user.deletedAt != null) throw AccountDeletedException()
        if (user.disabledAt != null) throw AccountDisabledException()
        if (!passwordEncoder.matches(password, user.password)) throw InvalidCredentialsException()
        return jwtService.generateToken(user.email, user.role)
    }

    @Transactional
    fun ensureAdmin(email: String, password: String) {
        val user = userRepository.findByEmail(email) ?: run {
            register(email, password)
            userRepository.findByEmail(email)!!
        }
        if (user.role == "ADMIN") return
        user.role = "ADMIN"
        userRepository.save(user)
        log.info("관리자 계정 준비 완료: {}", email)
    }
}
