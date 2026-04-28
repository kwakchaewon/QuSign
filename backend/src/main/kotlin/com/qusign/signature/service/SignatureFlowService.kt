package com.qusign.signature.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.auth.entity.User
import com.qusign.auth.repository.UserRepository
import com.qusign.auth.service.EncryptedKey
import com.qusign.auth.service.KeyEncryptionService
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
) {

    @Transactional
    fun requestSignature(requesterEmail: String, dto: CreateSignatureRequestDto): SignatureRequestResponse {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        val document = documentRepository.findByIdAndUser(dto.documentId, requester)
            ?: throw DocumentNotFoundException()

        val req = signatureRequestRepository.save(
            SignatureRequest(
                document = document,
                requester = requester,
                signerEmail = dto.signerEmail,
                token = UUID.randomUUID().toString(),
                expiresAt = LocalDateTime.now().plusHours(dto.expirationHours),
            )
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

    private fun signWithDecryptedKey(signer: User, password: String, message: ByteArray): ByteArray {
        val encryptedKey = objectMapper.readValue(signer.encryptedPrivateKey, EncryptedKey::class.java)
        val privateKeyBytes = keyEncryptionService.decrypt(encryptedKey, password)
        return try {
            val privateKey = KeyFactory.getInstance("ML-DSA", "BC")
                .generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
            pqcSignatureService.sign(privateKey, message)
        } finally {
            privateKeyBytes.fill(0)
        }
    }

    private fun sha3256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA3-256").digest(bytes)
}
