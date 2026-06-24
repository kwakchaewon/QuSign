package com.qusign.document.service

import com.qusign.auth.service.AuthService
// [SES보류] import com.qusign.common.email.EmailService
import com.qusign.common.storage.StorageService
import com.qusign.document.exception.AlreadySignedDocumentException
import com.qusign.document.exception.DocumentNotFoundException
import com.qusign.signature.service.PdfSignatureService
import com.qusign.signature.service.SignatureMetadata
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class DocumentServiceTest {

    @MockitoBean lateinit var storageService: StorageService
    // [SES보류] @MockitoBean lateinit var emailService: EmailService
    @MockitoBean lateinit var pdfSignatureService: PdfSignatureService
    @MockitoBean lateinit var redisMessageListenerContainer: RedisMessageListenerContainer

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        doNothing().whenever(storageService).upload(any(), any(), any())
        whenever(storageService.download(any())).thenReturn(byteArrayOf(1, 2, 3))
        whenever(pdfSignatureService.extractMetadata(any())).thenReturn(null)
    }

    private fun pdf(name: String = "test.pdf") =
        MockMultipartFile("file", name, "application/pdf", byteArrayOf(37, 80, 68, 70)) // %PDF

    @Test
    fun `PDF 업로드 성공`() {
        authService.register("upload@qusign.com", "password123")
        val result = documentService.upload("upload@qusign.com", pdf())

        assertNotNull(result.id)
        assertTrue(result.originalFilename == "test.pdf")
        assertTrue(result.hashSha3256.length == 64)
    }

    @Test
    fun `문서 목록 조회 성공`() {
        authService.register("list@qusign.com", "password123")
        documentService.upload("list@qusign.com", pdf("a.pdf"))
        documentService.upload("list@qusign.com", pdf("b.pdf"))

        val list = documentService.list("list@qusign.com")
        assertTrue(list.size == 2)
    }

    @Test
    fun `문서 다운로드 성공`() {
        authService.register("dl@qusign.com", "password123")
        val doc = documentService.upload("dl@qusign.com", pdf())

        val (bytes, filename) = documentService.download("dl@qusign.com", doc.id)
        assertTrue(bytes.isNotEmpty())
        assertTrue(filename == "test.pdf")
    }

    @Test
    fun `이미 서명된 PDF 업로드 시 예외`() {
        authService.register("signed@qusign.com", "password123")
        val fakeMetadata = SignatureMetadata(
            signature = byteArrayOf(1),
            signerId = "someone@qusign.com",
            signedAt = "2026-01-01T00:00:00",
            documentHash = byteArrayOf(1),
        )
        whenever(pdfSignatureService.extractMetadata(any())).thenReturn(fakeMetadata)

        assertThrows<AlreadySignedDocumentException> {
            documentService.upload("signed@qusign.com", pdf("already_signed.pdf"))
        }
    }

    @Test
    fun `타인 문서 다운로드 시 예외 발생`() {
        authService.register("owner@qusign.com", "password123")
        authService.register("other@qusign.com", "password123")
        val doc = documentService.upload("owner@qusign.com", pdf())

        assertThrows<DocumentNotFoundException> {
            documentService.download("other@qusign.com", doc.id)
        }
    }
}
