package com.qusign.auth.service

import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedKey(
    val ciphertext: String,
    val salt: String,
    val iv: String,
    val iterations: Int = PBKDF2_ITERATIONS,
)

private const val PBKDF2_ITERATIONS = 310_000
private const val KEY_LENGTH_BITS = 256
private const val GCM_TAG_LENGTH_BITS = 128
private const val SALT_BYTES = 16
private const val IV_BYTES = 12

@Service
class KeyEncryptionService {

    fun encrypt(plaintext: ByteArray, password: String): EncryptedKey {
        val salt = SecureRandom.getInstanceStrong().generateSeed(SALT_BYTES)
        val iv = SecureRandom.getInstanceStrong().generateSeed(IV_BYTES)
        val key = deriveKey(password, salt, PBKDF2_ITERATIONS)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

        return EncryptedKey(
            ciphertext = Base64.getEncoder().encodeToString(cipher.doFinal(plaintext)),
            salt = Base64.getEncoder().encodeToString(salt),
            iv = Base64.getEncoder().encodeToString(iv),
        )
    }

    fun decrypt(encryptedKey: EncryptedKey, password: String): ByteArray {
        val salt = Base64.getDecoder().decode(encryptedKey.salt)
        val iv = Base64.getDecoder().decode(encryptedKey.iv)
        val key = deriveKey(password, salt, encryptedKey.iterations)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

        return cipher.doFinal(Base64.getDecoder().decode(encryptedKey.ciphertext))
    }

    private fun deriveKey(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
