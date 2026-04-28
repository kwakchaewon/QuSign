package com.qusign.document.service

import com.qusign.auth.repository.UserRepository
import com.qusign.common.storage.StorageService
import com.qusign.document.dto.DocumentResponse
import com.qusign.document.entity.Document
import com.qusign.document.exception.DocumentNotFoundException
import com.qusign.document.exception.StorageException
import com.qusign.document.repository.DocumentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.security.MessageDigest
import java.util.UUID

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
) {
    @Transactional
    fun upload(email: String, file: MultipartFile): DocumentResponse {
        val user = userRepository.findByEmail(email) ?: throw DocumentNotFoundException()
        val bytes = file.bytes
        val hash = sha3256Hex(bytes)
        val key = "documents/${user.id}/${UUID.randomUUID()}/${file.originalFilename ?: "document.pdf"}"

        try {
            storageService.upload(key, bytes, file.contentType ?: "application/pdf")
        } catch (e: Exception) {
            throw StorageException("파일 업로드 실패", e)
        }

        val doc = documentRepository.save(
            Document(
                user = user,
                originalFilename = file.originalFilename ?: "document.pdf",
                storageKey = key,
                hashSha3256 = hash,
            )
        )
        return DocumentResponse(doc)
    }

    @Transactional(readOnly = true)
    fun list(email: String): List<DocumentResponse> {
        val user = userRepository.findByEmail(email) ?: return emptyList()
        return documentRepository.findByUserOrderByCreatedAtDesc(user).map { DocumentResponse(it) }
    }

    @Transactional(readOnly = true)
    fun download(email: String, id: Long): Pair<ByteArray, String> {
        val user = userRepository.findByEmail(email) ?: throw DocumentNotFoundException()
        val doc = documentRepository.findByIdAndUser(id, user) ?: throw DocumentNotFoundException()

        return try {
            storageService.download(doc.storageKey) to doc.originalFilename
        } catch (e: Exception) {
            throw StorageException("파일 다운로드 실패", e)
        }
    }

    private fun sha3256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA3-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
