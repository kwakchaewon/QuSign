package com.qusign.document.service

import com.qusign.auth.entity.User
import com.qusign.auth.repository.UserRepository
import com.qusign.common.storage.StorageService
import com.qusign.document.dto.DocumentResponse
import com.qusign.document.entity.Document
import com.qusign.document.exception.AlreadySignedDocumentException
import com.qusign.document.exception.BatchTooManyFilesException
import com.qusign.document.exception.DocumentNotFoundException
import com.qusign.document.exception.InvalidFileTypeException
import com.qusign.document.exception.StorageException
import com.qusign.document.repository.DocumentBundleItemRepository
import com.qusign.document.repository.DocumentRepository
import com.qusign.signature.repository.SignatureRequestRepository
import com.qusign.signature.service.PdfSignatureService
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
    private val signatureRequestRepository: SignatureRequestRepository,
    private val documentBundleItemRepository: DocumentBundleItemRepository,
    private val pdfSignatureService: PdfSignatureService,
) {
    @Transactional
    fun upload(email: String, file: MultipartFile): DocumentResponse {
        val user = userRepository.findByEmail(email) ?: throw DocumentNotFoundException()
        return uploadForUser(user, file)
    }

    @Transactional
    fun uploadBatch(email: String, files: List<MultipartFile>): List<DocumentResponse> {
        if (files.isEmpty()) throw DocumentNotFoundException()
        if (files.size > 5) throw BatchTooManyFilesException()
        val user = userRepository.findByEmail(email) ?: throw DocumentNotFoundException()
        return files.map { uploadForUser(user, it) }
    }

    private fun uploadForUser(user: User, file: MultipartFile): DocumentResponse {
        val bytes = file.bytes
        if (!isPdf(bytes)) throw InvalidFileTypeException()
        if (pdfSignatureService.extractMetadata(bytes) != null) throw AlreadySignedDocumentException()
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
        val docs = documentRepository.findByUserOrderByCreatedAtDesc(user)
        val requestsByDocId = signatureRequestRepository.findByDocumentIn(docs).groupBy { it.document.id }

        // 번들 정보 조회: 문서가 어떤 번들에 속하는지
        val bundleItemsByDocId = documentBundleItemRepository.findByDocumentIn(docs).groupBy { it.document.id }

        // 번들별 문서 수 계산
        val bundleDocCount = mutableMapOf<Long, Int>()
        val bundlePrimaryDocId = mutableMapOf<Long, Long>() // bundleId → primaryDocId (ordinal=0)
        bundleItemsByDocId.values.flatten().forEach { item ->
            val bundleId = item.bundle.id
            bundleDocCount[bundleId] = (bundleDocCount[bundleId] ?: 0) + 1
            if (item.ordinal == 0) bundlePrimaryDocId[bundleId] = item.document.id
        }

        return docs.map { doc ->
            val reqs = requestsByDocId[doc.id] ?: emptyList()
            val status = when {
                reqs.isEmpty() -> "NONE"
                reqs.all { it.status == "SIGNED" } -> "SIGNED"
                else -> "PENDING"
            }
            val bundleItem = bundleItemsByDocId[doc.id]?.firstOrNull()
            val bundleId = bundleItem?.bundle?.id
            val isPrimary = bundleId != null && bundlePrimaryDocId[bundleId] == doc.id
            DocumentResponse(
                id = doc.id,
                originalFilename = doc.originalFilename,
                hashSha3256 = doc.hashSha3256,
                createdAt = doc.createdAt?.toString(),
                signatureStatus = status,
                bundleId = bundleId,
                bundlePrimary = isPrimary,
                bundleDocCount = if (bundleId != null) bundleDocCount[bundleId] ?: 1 else 1,
            )
        }
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

    private fun isPdf(bytes: ByteArray): Boolean =
        bytes.size >= 4 &&
        bytes[0] == 0x25.toByte() && // %
        bytes[1] == 0x50.toByte() && // P
        bytes[2] == 0x44.toByte() && // D
        bytes[3] == 0x46.toByte()    // F

    private fun sha3256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA3-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
