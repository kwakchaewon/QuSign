package com.qusign.signature.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
class PqcSignatureServiceTest {

    @Autowired
    lateinit var pqcSignatureService: PqcSignatureService

    @Test
    fun `ML-DSA 서명 검증 성공`() {
        val keyPair = pqcSignatureService.generateKeyPair()
        val message = "테스트 문서 해시".toByteArray()
        val signature = pqcSignatureService.sign(keyPair.private, message)
        assertTrue(pqcSignatureService.verify(keyPair.public, message, signature))
    }

    @Test
    fun `변조된 메시지는 검증 실패`() {
        val keyPair = pqcSignatureService.generateKeyPair()
        val original = "원본 문서 해시".toByteArray()
        val signature = pqcSignatureService.sign(keyPair.private, original)
        val tampered = "변조된 문서 해시".toByteArray()
        assertFalse(pqcSignatureService.verify(keyPair.public, tampered, signature))
    }

    @Test
    fun `다른 키쌍으로는 검증 실패`() {
        val keyPair1 = pqcSignatureService.generateKeyPair()
        val keyPair2 = pqcSignatureService.generateKeyPair()
        val message = "테스트 문서 해시".toByteArray()
        val signature = pqcSignatureService.sign(keyPair1.private, message)
        assertFalse(pqcSignatureService.verify(keyPair2.public, message, signature))
    }
}
