package com.qusign.document.service

import com.qusign.auth.service.AuthService
import com.qusign.common.storage.StorageService
import com.qusign.document.exception.DocumentNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class DocumentServiceTest {

    @MockitoBean
    lateinit var storageService: StorageService

    @Autowired
    lateinit var documentService: DocumentService

    @Autowired
    lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        doNothing().whenever(storageService).upload(any(), any(), any())
        whenever(storageService.download(any())).thenReturn(byteArrayOf(1, 2, 3))
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
    fun `타인 문서 다운로드 시 예외 발생`() {
        authService.register("owner@qusign.com", "password123")
        authService.register("other@qusign.com", "password123")
        val doc = documentService.upload("owner@qusign.com", pdf())

        assertThrows<DocumentNotFoundException> {
            documentService.download("other@qusign.com", doc.id)
        }
    }
}
