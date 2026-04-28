package com.qusign.signature.service

import com.qusign.auth.service.AuthService
import com.qusign.common.storage.StorageService
import com.qusign.document.service.DocumentService
import com.qusign.signature.dto.CreateSignatureRequestDto
import com.qusign.signature.dto.VerifyResponse
import com.qusign.signature.exception.SignatureRequestAlreadySignedException
import com.qusign.signature.exception.SignatureRequestNotFoundException
import com.qusign.signature.exception.UnauthorizedSignerException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class SignatureFlowServiceTest {

    @MockitoBean
    lateinit var storageService: StorageService

    @Autowired lateinit var authService: AuthService
    @Autowired lateinit var documentService: DocumentService
    @Autowired lateinit var signatureFlowService: SignatureFlowService

    private val pdfBytes: ByteArray by lazy { createMinimalPdf() }
    private val storedFiles = mutableMapOf<String, ByteArray>()

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
        PDDocument().use { doc ->
            doc.addPage(PDPage())
            doc.save(out)
        }
        return out.toByteArray()
    }

    private fun pdf(name: String = "doc.pdf") =
        MockMultipartFile("file", name, "application/pdf", pdfBytes)

    @Test
    fun `서명 요청 생성 성공`() {
        authService.register("req@qusign.com", "pw1234!")
        authService.register("signer@qusign.com", "pw1234!")
        val doc = documentService.upload("req@qusign.com", pdf())

        val result = signatureFlowService.requestSignature(
            "req@qusign.com",
            CreateSignatureRequestDto(documentId = doc.id, signerEmail = "signer@qusign.com"),
        )

        assertNotNull(result.token)
        assertTrue(result.status == "PENDING")
        assertTrue(result.signerEmail == "signer@qusign.com")
    }

    @Test
    fun `서명 및 검증 성공`() {
        authService.register("req2@qusign.com", "pw1234!")
        authService.register("signer2@qusign.com", "pw1234!")
        val doc = documentService.upload("req2@qusign.com", pdf())

        val req = signatureFlowService.requestSignature(
            "req2@qusign.com",
            CreateSignatureRequestDto(documentId = doc.id, signerEmail = "signer2@qusign.com"),
        )

        val signed = signatureFlowService.sign(req.token, "signer2@qusign.com", "pw1234!")
        assertNotNull(signed.id)

        val verifyResult: VerifyResponse = signatureFlowService.verify(req.token)
        assertTrue(verifyResult.valid)
        assertTrue(verifyResult.signerId == "signer2@qusign.com")
        assertNotNull(verifyResult.documentHash)
    }

    @Test
    fun `잘못된 토큰으로 서명 시 예외`() {
        authService.register("signer3@qusign.com", "pw1234!")
        assertThrows<SignatureRequestNotFoundException> {
            signatureFlowService.sign("invalid-token", "signer3@qusign.com", "pw1234!")
        }
    }

    @Test
    fun `이중 서명 시 예외`() {
        authService.register("req4@qusign.com", "pw1234!")
        authService.register("signer4@qusign.com", "pw1234!")
        val doc = documentService.upload("req4@qusign.com", pdf())
        val req = signatureFlowService.requestSignature(
            "req4@qusign.com",
            CreateSignatureRequestDto(documentId = doc.id, signerEmail = "signer4@qusign.com"),
        )

        signatureFlowService.sign(req.token, "signer4@qusign.com", "pw1234!")

        assertThrows<SignatureRequestAlreadySignedException> {
            signatureFlowService.sign(req.token, "signer4@qusign.com", "pw1234!")
        }
    }

    @Test
    fun `서명자 불일치 시 예외`() {
        authService.register("req5@qusign.com", "pw1234!")
        authService.register("signer5@qusign.com", "pw1234!")
        authService.register("other5@qusign.com", "pw1234!")
        val doc = documentService.upload("req5@qusign.com", pdf())
        val req = signatureFlowService.requestSignature(
            "req5@qusign.com",
            CreateSignatureRequestDto(documentId = doc.id, signerEmail = "signer5@qusign.com"),
        )

        assertThrows<UnauthorizedSignerException> {
            signatureFlowService.sign(req.token, "other5@qusign.com", "pw1234!")
        }
    }
}
