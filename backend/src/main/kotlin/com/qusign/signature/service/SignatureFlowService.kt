package com.qusign.signature.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.auth.entity.User
import com.qusign.auth.repository.UserRepository
import com.qusign.auth.service.EncryptedKey
import com.qusign.auth.service.KeyEncryptionService
import com.qusign.common.email.EmailService
import com.qusign.common.storage.StorageService
import com.qusign.document.exception.DocumentNotFoundException
import com.qusign.document.repository.DocumentRepository
import com.qusign.signature.dto.*
import com.qusign.signature.entity.Signature
import com.qusign.signature.entity.SignatureRequest
import com.qusign.signature.exception.*
import com.qusign.signature.repository.SignatureRepository
import com.qusign.signature.repository.SignatureRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

@Service
class SignatureFlowService(
    private val signatureRequestRepository: SignatureRequestRepository,
    private val signatureRepository: SignatureRepository,
    private val documentRepository: DocumentRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    private val pqcSignatureService: PqcSignatureService,
    private val pdfSignatureService: PdfSignatureService,
    private val keyEncryptionService: KeyEncryptionService,
    private val objectMapper: ObjectMapper,
    private val emailService: EmailService,
) {

    @Transactional
    fun requestSignature(requesterEmail: String, dto: CreateSignatureRequestDto): SignatureRequestResponse {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        return requestSignatureForUser(requester, dto)
    }

    @Transactional
    fun requestSignatureBatch(requesterEmail: String, dto: BatchCreateSignatureRequestDto): List<SignatureRequestResponse> {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        return dto.requests.map { requestSignatureForUser(requester, it) }
    }

    private fun requestSignatureForUser(requester: User, dto: CreateSignatureRequestDto): SignatureRequestResponse {
        val document = documentRepository.findByIdAndUser(dto.documentId, requester)
            ?: throw DocumentNotFoundException()

        val existing = signatureRequestRepository.findByDocumentAndSignerEmail(document, dto.signerEmail)
        if (existing != null && existing.status == "PENDING" && existing.expiresAt.isAfter(LocalDateTime.now())) {
            throw DuplicateSignatureRequestException()
        }

        val req = signatureRequestRepository.save(
            SignatureRequest(
                document = document,
                requester = requester,
                signerEmail = dto.signerEmail,
                token = UUID.randomUUID().toString(),
                expiresAt = LocalDateTime.now().plusHours(dto.expirationHours),
            )
        )

        emailService.sendSignatureRequest(
            to = req.signerEmail,
            token = req.token,
            documentName = document.originalFilename,
            requesterEmail = requester.email,
            expiresAt = req.expiresAt.toString(),
        )

        return SignatureRequestResponse(req)
    }

    @Transactional
    fun sign(token: String, signerEmail: String, password: String): SignatureResponse {
        val req = signatureRequestRepository.findByToken(token)
            ?: throw SignatureRequestNotFoundException()

        if (req.expiresAt.isBefore(LocalDateTime.now())) throw SignatureRequestExpiredException()
        if (req.status == "SIGNED") throw SignatureRequestAlreadySignedException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()

        val signer = userRepository.findByEmail(signerEmail) ?: throw UnauthorizedSignerException()

        val originalPdfBytes = storageService.download(req.document.storageKey)
        val documentHash = sha3256(originalPdfBytes)

        val signatureBytes = signWithDecryptedKey(signer, password, documentHash)

        val signedPdfBytes = pdfSignatureService.embedSignature(
            pdfBytes = originalPdfBytes,
            signature = signatureBytes,
            signerId = signerEmail,
            documentHash = documentHash,
        )

        val signedKey = "signed-documents/${req.id}/${req.document.originalFilename}"
        storageService.upload(signedKey, signedPdfBytes, "application/pdf")

        val signature = signatureRepository.save(
            Signature(
                signatureRequest = req,
                signer = signer,
                signedStorageKey = signedKey,
                signatureValue = Base64.getEncoder().encodeToString(signatureBytes),
            )
        )

        req.status = "SIGNED"
        return SignatureResponse(signature)
    }

    @Transactional(readOnly = true)
    fun verify(token: String): VerifyResponse {
        val req = signatureRequestRepository.findByToken(token)
            ?: throw SignatureRequestNotFoundException()

        val signature = signatureRepository.findBySignatureRequest(req)
            ?: return VerifyResponse(valid = false)

        val signedPdfBytes = storageService.download(signature.signedStorageKey)
        val metadata = pdfSignatureService.extractMetadata(signedPdfBytes)
            ?: return VerifyResponse(valid = false)

        val signer = userRepository.findByEmail(metadata.signerId)
            ?: return VerifyResponse(valid = false)

        val pubKeyBytes = Base64.getDecoder().decode(signer.publicKey)
        val publicKey = KeyFactory.getInstance("ML-DSA", "BC")
            .generatePublic(X509EncodedKeySpec(pubKeyBytes))

        val valid = try {
            pqcSignatureService.verify(publicKey, metadata.documentHash, metadata.signature)
        } catch (e: Exception) {
            false
        }

        return VerifyResponse(
            valid = valid,
            signerId = metadata.signerId,
            signedAt = metadata.signedAt,
            documentHash = metadata.documentHash.joinToString("") { "%02x".format(it) },
        )
    }

    @Transactional(readOnly = true)
    fun verifyFile(pdfBytes: ByteArray): VerifyResponse {
        val metadata = pdfSignatureService.extractMetadata(pdfBytes)
            ?: throw NoQuSignMetadataException()

        val signer = userRepository.findByEmail(metadata.signerId)
            ?: return VerifyResponse(valid = false, signerId = metadata.signerId)

        val pubKeyBytes = Base64.getDecoder().decode(signer.publicKey)
        val publicKey = KeyFactory.getInstance("ML-DSA", "BC")
            .generatePublic(X509EncodedKeySpec(pubKeyBytes))

        val valid = try {
            pqcSignatureService.verify(publicKey, metadata.documentHash, metadata.signature)
        } catch (e: Exception) {
            false
        }

        return VerifyResponse(
            valid = valid,
            signerId = metadata.signerId,
            signedAt = metadata.signedAt,
            documentHash = metadata.documentHash.joinToString("") { "%02x".format(it) },
        )
    }

    private fun signWithDecryptedKey(signer: User, password: String, message: ByteArray): ByteArray {
        val encryptedKey = objectMapper.readValue(signer.encryptedPrivateKey, EncryptedKey::class.java)
        val privateKeyBytes = try {
            keyEncryptionService.decrypt(encryptedKey, password)
        } catch (e: Exception) {
            throw InvalidSignaturePasswordException()
        }
        return try {
            val privateKey = KeyFactory.getInstance("ML-DSA", "BC")
                .generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
            pqcSignatureService.sign(privateKey, message)
        } finally {
            privateKeyBytes.fill(0)
        }
    }

    @Transactional(readOnly = true)
    fun getSignedDocument(token: String, signerEmail: String): Pair<ByteArray, String> {
        val req = signatureRequestRepository.findByToken(token)
            ?: throw SignatureRequestNotFoundException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
        val signature = signatureRepository.findBySignatureRequest(req)
            ?: throw SignatureRequestNotFoundException()
        val bytes = storageService.download(signature.signedStorageKey)
        return Pair(bytes, req.document.originalFilename.addQusignedSuffix())
    }

    @Transactional(readOnly = true)
    fun getDetail(documentId: Long, requesterEmail: String): SignatureRequestDetailResponse {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        val document = documentRepository.findByIdAndUser(documentId, requester)
            ?: throw DocumentNotFoundException()

        val requests = signatureRequestRepository.findByDocumentOrderByCreatedAtAsc(document)
        val signaturesByRequestId = signatureRepository
            .findBySignatureRequestIn(requests)
            .associateBy { it.signatureRequest.id }

        val now = java.time.LocalDateTime.now()
        val signers = requests.map { req ->
            val sig = signaturesByRequestId[req.id]
            val effectiveStatus = when {
                req.status == "SIGNED" -> "SIGNED"
                req.expiresAt.isBefore(now) -> "EXPIRED"
                else -> "PENDING"
            }
            SignerDetailDto(
                email = req.signerEmail,
                status = effectiveStatus,
                signedAt = sig?.signedAt?.toString(),
                signatureToken = if (effectiveStatus == "PENDING") req.token else null,
            )
        }

        val first = requests.firstOrNull()
        return SignatureRequestDetailResponse(
            id = documentId,
            documentName = document.originalFilename,
            hashSha3256 = document.hashSha3256,
            uploadedAt = document.createdAt?.toString() ?: "",
            requesterEmail = requester.email,
            algorithm = "ML-DSA-65",
            requestedAt = first?.createdAt?.toString() ?: "",
            expiresAt = first?.expiresAt?.toString() ?: "",
            signers = signers,
        )
    }

    @Transactional(readOnly = true)
    fun getSignedDocumentByRequester(documentId: Long, signerEmail: String, requesterEmail: String): Pair<ByteArray, String> {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        val document = documentRepository.findByIdAndUser(documentId, requester)
            ?: throw DocumentNotFoundException()
        val req = signatureRequestRepository.findByDocumentAndSignerEmail(document, signerEmail)
            ?: throw SignatureRequestNotFoundException()
        if (req.status != "SIGNED") throw SignatureRequestNotFoundException()
        val signature = signatureRepository.findBySignatureRequest(req)
            ?: throw SignatureRequestNotFoundException()
        val bytes = storageService.download(signature.signedStorageKey)
        return Pair(bytes, document.originalFilename.addQusignedSuffix())
    }

    @Transactional(readOnly = true)
    fun getDocument(token: String, signerEmail: String): Pair<ByteArray, String> {
        val req = signatureRequestRepository.findByToken(token)
            ?: throw SignatureRequestNotFoundException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
        val bytes = storageService.download(req.document.storageKey)
        return Pair(bytes, req.document.originalFilename)
    }

    private fun sha3256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA3-256").digest(bytes)
}

private fun String.addQusignedSuffix(): String {
    val dot = lastIndexOf('.')
    return if (dot != -1) substring(0, dot) + "_qusigned" + substring(dot)
    else this + "_qusigned"
}
