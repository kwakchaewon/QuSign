package com.qusign.audit.service

import com.qusign.audit.entity.AuditEventType
import com.qusign.audit.repository.AuditLogRepository
import com.qusign.auth.service.AuthService
import com.qusign.common.audit.AuditContext
import com.qusign.common.email.EmailService
import com.qusign.common.storage.StorageService
import com.qusign.document.service.DocumentService
import com.qusign.notification.service.NotificationService
import com.qusign.signature.dto.CreateSignatureRequestDto
import com.qusign.signature.service.SignatureFlowService
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class AuditLogServiceTest {

    @MockitoBean lateinit var storageService: StorageService
    @MockitoBean lateinit var emailService: EmailService
    @MockitoBean lateinit var notificationService: NotificationService
    @MockitoBean lateinit var redisMessageListenerContainer: RedisMessageListenerContainer

    @Autowired lateinit var auditLogService: AuditLogService
    @Autowired lateinit var auditLogRepository: AuditLogRepository
    @Autowired lateinit var authService: AuthService
    @Autowired lateinit var documentService: DocumentService
    @Autowired lateinit var signatureFlowService: SignatureFlowService

    private val pdfBytes: ByteArray by lazy { createMinimalPdf() }
    private val storedFiles = mutableMapOf<String, ByteArray>()
    private val testCtx = AuditContext(ipAddress = "10.0.0.1", userAgent = "JUnit/5")

    @BeforeEach
    fun setUp() {
        storedFiles.clear()
        doAnswer { inv ->
            storedFiles[inv.getArgument(0)] = inv.getArgument(1)
        }.whenever(storageService).upload(any(), any(), any())
        whenever(storageService.download(any())).thenAnswer { inv ->
            storedFiles[inv.getArgument(0)] ?: pdfBytes
        }
    }

    private fun createMinimalPdf(): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        PDDocument().use { doc -> doc.addPage(PDPage()); doc.save(out) }
        return out.toByteArray()
    }

    private fun pdf(name: String = "doc.pdf") =
        MockMultipartFile("file", name, "application/pdf", pdfBytes)

    @Test
    fun `record - 각 이벤트 타입 저장 성공`() {
        AuditEventType.entries.forEach { eventType ->
            auditLogService.record(
                eventType = eventType,
                actorEmail = "actor@test.com",
                auditCtx = testCtx,
            )
        }
        val saved = auditLogRepository.findAll()
        assertEquals(AuditEventType.entries.size, saved.size)
        assertTrue(saved.all { it.ipAddress == "10.0.0.1" })
        assertTrue(saved.all { it.userAgent == "JUnit/5" })
    }

    @Test
    fun `record - 삭제 메서드 미노출 확인`() {
        // AuditLogService 에 delete / deleteById 등의 메서드가 없어야 한다
        val methods = AuditLogService::class.java.methods.map { it.name }
        assertFalse(methods.any { it.startsWith("delete") || it.startsWith("remove") })
    }

    @Test
    fun `findByDocumentId - 서명 요청 후 로그 조회`() {
        authService.register("req-a@qusign.com", "pw1234!")
        authService.register("sgn-a@qusign.com", "pw1234!")
        val doc = documentService.upload("req-a@qusign.com", pdf())

        signatureFlowService.requestSignature(
            "req-a@qusign.com",
            CreateSignatureRequestDto(documentId = doc.id, signerEmail = "sgn-a@qusign.com"),
            testCtx,
        )

        val logs = auditLogService.findByDocumentId(doc.id)
        assertTrue(logs.isNotEmpty())
        assertEquals(AuditEventType.SIGN_REQUEST_CREATED, logs.first().eventType)
        assertEquals("req-a@qusign.com", logs.first().actorEmail)
    }

    @Test
    fun `findByDocumentId - 서명 완료 후 SIGNED 이벤트 존재`() {
        authService.register("req-b@qusign.com", "pw1234!")
        authService.register("sgn-b@qusign.com", "pw1234!")
        val doc = documentService.upload("req-b@qusign.com", pdf())

        val req = signatureFlowService.requestSignature(
            "req-b@qusign.com",
            CreateSignatureRequestDto(documentId = doc.id, signerEmail = "sgn-b@qusign.com"),
            testCtx,
        )

        signatureFlowService.sign(req.token, "sgn-b@qusign.com", "pw1234!", testCtx)

        val logs = auditLogService.findByDocumentId(doc.id)
        val eventTypes = logs.map { it.eventType }
        assertTrue(AuditEventType.SIGN_REQUEST_CREATED in eventTypes)
        assertTrue(AuditEventType.SIGNED in eventTypes)
    }

    @Test
    fun `IP 주소 길이 초과 시 45자로 잘라 저장`() {
        val longIp = "a".repeat(100)
        auditLogService.record(
            eventType = AuditEventType.SIGNED,
            actorEmail = "x@test.com",
            auditCtx = AuditContext(ipAddress = longIp, userAgent = "ua"),
        )
        val saved = auditLogRepository.findAll().last()
        assertEquals(45, saved.ipAddress.length)
    }
}
