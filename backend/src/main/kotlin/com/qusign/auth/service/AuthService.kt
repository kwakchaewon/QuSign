package com.qusign.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.auth.entity.User
import com.qusign.auth.exception.EmailAlreadyExistsException
import com.qusign.auth.exception.InvalidCredentialsException
import com.qusign.auth.repository.UserRepository
import com.qusign.signature.service.PqcSignatureService
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
        if (!passwordEncoder.matches(password, user.password)) throw InvalidCredentialsException()
        return jwtService.generateToken(email)
    }
}
