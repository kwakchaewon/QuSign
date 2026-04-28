package com.qusign.signature.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class PdfSignatureServiceTest {

    @Autowired lateinit var pdfSignatureService: PdfSignatureService
    @Autowired lateinit var pqcSignatureService: PqcSignatureService

    private fun createMinimalPdf(): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        PDDocument().use { doc ->
            doc.addPage(PDPage())
            doc.save(out)
        }
        return out.toByteArray()
    }

    @Test
    fun `PDF 서명값 삽입 후 추출 성공`() {
        val pdf = createMinimalPdf()
        val keyPair = pqcSignatureService.generateKeyPair()
        val message = "테스트 문서 해시".toByteArray()
        val signature = pqcSignatureService.sign(keyPair.private, message)

        val docHash = "테스트 문서 해시".toByteArray()
        val signedPdf = pdfSignatureService.embedSignature(pdf, signature, "tester@qusign.com", docHash)
        val extracted = pdfSignatureService.extractSignature(signedPdf)

        assertNotNull(extracted)
        assertContentEquals(signature, extracted)
    }

    @Test
    fun `PDF 삽입 후 추출 서명값으로 ML-DSA 검증 통과`() {
        val pdf = createMinimalPdf()
        val keyPair = pqcSignatureService.generateKeyPair()
        val message = "테스트 문서 해시".toByteArray()
        val signature = pqcSignatureService.sign(keyPair.private, message)

        val signedPdf = pdfSignatureService.embedSignature(pdf, signature, "tester@qusign.com", message)
        val extracted = pdfSignatureService.extractSignature(signedPdf)!!

        assertTrue(pqcSignatureService.verify(keyPair.public, message, extracted))
    }

    @Test
    fun `서명 메타데이터 추출 성공`() {
        val pdf = createMinimalPdf()
        val docHash = "hash".toByteArray()
        val signature = pqcSignatureService.sign(
            pqcSignatureService.generateKeyPair().private,
            docHash
        )

        val signedPdf = pdfSignatureService.embedSignature(pdf, signature, "tester@qusign.com", docHash)
        val meta = pdfSignatureService.extractMetadata(signedPdf)

        assertNotNull(meta)
        assertTrue(meta.signerId == "tester@qusign.com")
        assertTrue(meta.signedAt.isNotBlank())
        assertContentEquals(docHash, meta.documentHash)
    }

    @Test
    fun `서명 없는 PDF는 추출 결과 null`() {
        val pdf = createMinimalPdf()
        assertNull(pdfSignatureService.extractSignature(pdf))
    }
}
