package com.qusign.signature.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.qusign.auth.entity.User
import com.qusign.auth.repository.UserRepository
import com.qusign.auth.service.EncryptedKey
import com.qusign.auth.service.KeyEncryptionService
import com.qusign.common.email.EmailService
import com.qusign.common.storage.StorageService
import com.qusign.document.entity.DocumentBundle
import com.qusign.document.entity.DocumentBundleItem
import com.qusign.document.exception.DocumentNotFoundException
import com.qusign.document.repository.DocumentBundleItemRepository
import com.qusign.document.repository.DocumentBundleRepository
import com.qusign.document.repository.DocumentRepository
import com.qusign.notification.service.NotificationService
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
    private val documentBundleRepository: DocumentBundleRepository,
    private val documentBundleItemRepository: DocumentBundleItemRepository,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    private val pqcSignatureService: PqcSignatureService,
    private val pdfSignatureService: PdfSignatureService,
    private val keyEncryptionService: KeyEncryptionService,
    private val objectMapper: ObjectMapper,
    private val emailService: EmailService,
    private val notificationService: NotificationService,
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
        if (existing != null) {
            if (existing.status == "SIGNED") throw SignatureRequestAlreadySignedException()
            if (existing.status == "PENDING" && existing.expiresAt.isAfter(LocalDateTime.now())) throw DuplicateSignatureRequestException()
        }

        val req = signatureRequestRepository.save(
            SignatureRequest(
                document = document,
                requester = requester,
                signerEmail = dto.signerEmail,
                token = UUID.randomUUID().toString(),
                expiresAt = LocalDateTime.now().plusHours(dto.expirationHours),
                message = dto.message?.takeIf { it.isNotBlank() },
            )
        )

        emailService.sendSignatureRequest(
            to = req.signerEmail,
            token = req.token,
            documentName = document.originalFilename,
            requesterEmail = requester.email,
            expiresAt = req.expiresAt.toString(),
        )

        val signer = userRepository.findByEmail(req.signerEmail)
        if (signer != null) {
            notificationService.createAndPublish(
                userId = signer.id,
                type = NotificationService.TYPE_SIGN_REQUEST,
                title = "서명 요청",
                message = "${requester.email}님이 '${document.originalFilename}' 서명을 요청했습니다.",
                referenceId = req.id,
                referenceToken = req.token,
            )
        }

        return SignatureRequestResponse(req)
    }

    // ── 번들 서명 요청 생성 ──────────────────────────────────────────────────
    @Transactional
    fun requestBundleSignature(requesterEmail: String, dto: CreateBundleSignatureRequestDto): BundleSignatureRequestResponse {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()

        val documents = dto.documentIds.mapIndexed { idx, docId ->
            documentRepository.findByIdAndUser(docId, requester)
                ?: throw DocumentNotFoundException()
        }

        // 번들 엔티티 생성
        val bundle = documentBundleRepository.save(DocumentBundle(owner = requester))
        documents.forEachIndexed { idx, doc ->
            documentBundleItemRepository.save(DocumentBundleItem(bundle = bundle, document = doc, ordinal = idx))
        }

        val signerLinks = mutableListOf<BundleSignerLinkDto>()
        val expiresAt = LocalDateTime.now().plusHours(dto.expirationHours)
        val primaryDocName = documents.first().originalFilename

        for (signerEmail in dto.signerEmails) {
            val bundleToken = UUID.randomUUID().toString()

            // 번들 내 문서별로 SignatureRequest 생성 (같은 bundleToken 공유)
            documents.forEach { doc ->
                signatureRequestRepository.save(
                    SignatureRequest(
                        document = doc,
                        requester = requester,
                        signerEmail = signerEmail,
                        token = UUID.randomUUID().toString(), // 개별 unique token (내부용)
                        expiresAt = expiresAt,
                        message = dto.message?.takeIf { it.isNotBlank() },
                        bundle = bundle,
                        bundleToken = bundleToken,
                    )
                )
            }

            // 서명자에게 이메일 발송 (번들 토큰 사용)
            val displayName = if (documents.size > 1)
                "${primaryDocName} 외 ${documents.size - 1}건"
            else primaryDocName

            emailService.sendSignatureRequest(
                to = signerEmail,
                token = bundleToken,
                documentName = displayName,
                requesterEmail = requester.email,
                expiresAt = expiresAt.toString(),
            )

            val signer = userRepository.findByEmail(signerEmail)
            if (signer != null) {
                notificationService.createAndPublish(
                    userId = signer.id,
                    type = NotificationService.TYPE_SIGN_REQUEST,
                    title = "서명 요청",
                    message = "${requester.email}님이 '$displayName' 서명을 요청했습니다.",
                    referenceId = bundle.id,
                    referenceToken = bundleToken,
                )
            }

            signerLinks.add(BundleSignerLinkDto(email = signerEmail, bundleToken = bundleToken))
        }

        return BundleSignatureRequestResponse(bundleId = bundle.id, signers = signerLinks)
    }

    // ── 서명 (단건 + 번들 통합) ─────────────────────────────────────────────
    @Transactional
    fun sign(token: String, signerEmail: String, password: String): SignatureResponse {
        // 번들 토큰으로 먼저 조회
        val bundleRequests = signatureRequestRepository
            .findByBundleTokenAndSignerEmailOrderByDocumentIdAsc(token, signerEmail)

        if (bundleRequests.isNotEmpty()) {
            return signBundle(bundleRequests, signerEmail, password)
        }

        // 단건 토큰 조회 (기존 로직)
        val req = signatureRequestRepository.findByToken(token)
            ?: throw SignatureRequestNotFoundException()

        if (req.status == "CANCELLED") throw SignatureRequestCancelledException()
        if (req.expiresAt.isBefore(LocalDateTime.now())) throw SignatureRequestExpiredException()
        if (req.status == "SIGNED") throw SignatureRequestAlreadySignedException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()

        val signer = userRepository.findByEmail(signerEmail) ?: throw UnauthorizedSignerException()
        val signatureBytes = decryptAndSign(signer, password, req.document)

        val signedPdfBytes = signDocument(req.document.storageKey, signatureBytes, signerEmail)
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

        notificationService.createAndPublish(
            userId = req.requester.id,
            type = NotificationService.TYPE_SIGN_DONE,
            title = "서명 완료",
            message = "${signerEmail}님이 '${req.document.originalFilename}'에 서명을 완료했습니다.",
            referenceId = req.document.id,
        )

        return SignatureResponse(signature)
    }

    private fun signBundle(requests: List<SignatureRequest>, signerEmail: String, password: String): SignatureResponse {
        val first = requests.first()

        if (first.status == "CANCELLED") throw SignatureRequestCancelledException()
        if (first.expiresAt.isBefore(LocalDateTime.now())) throw SignatureRequestExpiredException()
        if (first.status == "SIGNED") throw SignatureRequestAlreadySignedException()
        if (!first.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()

        val signer = userRepository.findByEmail(signerEmail) ?: throw UnauthorizedSignerException()

        var lastSignature: Signature? = null
        for (req in requests) {
            val signatureBytes = decryptAndSign(signer, password, req.document)
            val signedPdfBytes = signDocument(req.document.storageKey, signatureBytes, signerEmail)
            val signedKey = "signed-documents/${req.id}/${req.document.originalFilename}"
            storageService.upload(signedKey, signedPdfBytes, "application/pdf")

            lastSignature = signatureRepository.save(
                Signature(
                    signatureRequest = req,
                    signer = signer,
                    signedStorageKey = signedKey,
                    signatureValue = Base64.getEncoder().encodeToString(signatureBytes),
                )
            )
            req.status = "SIGNED"
        }

        val primaryDocName = requests.first().document.originalFilename
        val displayName = if (requests.size > 1) "$primaryDocName 외 ${requests.size - 1}건" else primaryDocName

        notificationService.createAndPublish(
            userId = first.requester.id,
            type = NotificationService.TYPE_SIGN_DONE,
            title = "서명 완료",
            message = "${signerEmail}님이 '$displayName'에 서명을 완료했습니다.",
            referenceId = first.bundle?.id ?: first.document.id,
        )

        return SignatureResponse(lastSignature!!)
    }

    // ── 서명자용 정보 조회 (단건 + 번들 통합) ─────────────────────────────────
    @Transactional(readOnly = true)
    fun getSignerRequestInfo(token: String, signerEmail: String): SignerRequestInfoResponse {
        // 번들 토큰으로 먼저 조회
        val bundleRequests = signatureRequestRepository
            .findByBundleTokenAndSignerEmailOrderByDocumentIdAsc(token, signerEmail)

        if (bundleRequests.isNotEmpty()) {
            val first = bundleRequests.first()
            if (!first.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
            val docs = bundleRequests.mapIndexed { idx, req ->
                BundleDocumentInfoDto(
                    index = idx,
                    filename = req.document.originalFilename,
                    hashSha3256 = req.document.hashSha3256,
                )
            }
            return SignerRequestInfoResponse(
                documentName = first.document.originalFilename,
                requesterEmail = first.requester.email,
                message = first.message,
                requestedAt = first.createdAt?.toString() ?: "",
                expiresAt = first.expiresAt.toString(),
                hashSha3256 = first.document.hashSha3256,
                cancelled = first.status == "CANCELLED",
                signed = first.status == "SIGNED",
                documents = docs,
                isBundle = true,
            )
        }

        // 단건 토큰 조회 (기존 로직)
        val req = signatureRequestRepository.findByToken(token)
            ?: throw SignatureRequestNotFoundException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
        return SignerRequestInfoResponse(
            documentName = req.document.originalFilename,
            requesterEmail = req.requester.email,
            message = req.message,
            requestedAt = req.createdAt?.toString() ?: "",
            expiresAt = req.expiresAt.toString(),
            hashSha3256 = req.document.hashSha3256,
            cancelled = req.status == "CANCELLED",
            signed = req.status == "SIGNED",
            documents = listOf(
                BundleDocumentInfoDto(0, req.document.originalFilename, req.document.hashSha3256)
            ),
            isBundle = false,
        )
    }

    // ── 서명자용 원본 PDF 다운로드 ──────────────────────────────────────────
    @Transactional(readOnly = true)
    fun getDocument(token: String, signerEmail: String): Pair<ByteArray, String> {
        // 번들 토큰: 첫 번째 문서 반환
        val bundleRequests = signatureRequestRepository
            .findByBundleTokenAndSignerEmailOrderByDocumentIdAsc(token, signerEmail)
        if (bundleRequests.isNotEmpty()) {
            val req = bundleRequests.first()
            if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
            val bytes = storageService.download(req.document.storageKey)
            return Pair(bytes, req.document.originalFilename)
        }

        val req = signatureRequestRepository.findByToken(token)
            ?: throw SignatureRequestNotFoundException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
        val bytes = storageService.download(req.document.storageKey)
        return Pair(bytes, req.document.originalFilename)
    }

    // ── 번들 내 특정 원본 PDF 다운로드 (index 지정) ─────────────────────────
    @Transactional(readOnly = true)
    fun getBundleDocument(bundleToken: String, signerEmail: String, index: Int): Pair<ByteArray, String> {
        val requests = signatureRequestRepository
            .findByBundleTokenAndSignerEmailOrderByDocumentIdAsc(bundleToken, signerEmail)
        if (requests.isEmpty()) throw SignatureRequestNotFoundException()
        val req = requests.getOrNull(index) ?: throw SignatureRequestNotFoundException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
        return Pair(storageService.download(req.document.storageKey), req.document.originalFilename)
    }

    // ── 서명자용 서명된 PDF 다운로드 ─────────────────────────────────────────
    @Transactional(readOnly = true)
    fun getSignedDocument(token: String, signerEmail: String): Pair<ByteArray, String> {
        // 번들 토큰: 첫 번째 서명 문서 반환
        val bundleRequests = signatureRequestRepository
            .findByBundleTokenAndSignerEmailOrderByDocumentIdAsc(token, signerEmail)
        if (bundleRequests.isNotEmpty()) {
            val req = bundleRequests.first()
            if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
            val signature = signatureRepository.findBySignatureRequest(req)
                ?: throw SignatureRequestNotFoundException()
            val bytes = storageService.download(signature.signedStorageKey)
            return Pair(bytes, req.document.originalFilename.addQusignedSuffix())
        }

        val req = signatureRequestRepository.findByToken(token)
            ?: throw SignatureRequestNotFoundException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
        val signature = signatureRepository.findBySignatureRequest(req)
            ?: throw SignatureRequestNotFoundException()
        val bytes = storageService.download(signature.signedStorageKey)
        return Pair(bytes, req.document.originalFilename.addQusignedSuffix())
    }

    // ── 번들 내 특정 서명된 PDF 다운로드 (서명자용, index 지정) ──────────────
    @Transactional(readOnly = true)
    fun getSignedBundleDocument(bundleToken: String, signerEmail: String, index: Int): Pair<ByteArray, String> {
        val requests = signatureRequestRepository
            .findByBundleTokenAndSignerEmailOrderByDocumentIdAsc(bundleToken, signerEmail)
        if (requests.isEmpty()) throw SignatureRequestNotFoundException()
        val req = requests.getOrNull(index) ?: throw SignatureRequestNotFoundException()
        if (!req.signerEmail.equals(signerEmail, ignoreCase = true)) throw UnauthorizedSignerException()
        if (req.status != "SIGNED") throw SignatureRequestNotFoundException()
        val signature = signatureRepository.findBySignatureRequest(req)
            ?: throw SignatureRequestNotFoundException()
        val bytes = storageService.download(signature.signedStorageKey)
        return Pair(bytes, req.document.originalFilename.addQusignedSuffix())
    }

    // ── 요청자용 상세 조회 (단건 문서 기준) ─────────────────────────────────
    @Transactional(readOnly = true)
    fun getDetail(documentId: Long, requesterEmail: String): SignatureRequestDetailResponse {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        val document = documentRepository.findByIdAndUser(documentId, requester)
            ?: throw DocumentNotFoundException()

        val requests = signatureRequestRepository.findByDocumentOrderByCreatedAtAsc(document)
        val signaturesByRequestId = signatureRepository
            .findBySignatureRequestIn(requests)
            .associateBy { it.signatureRequest.id }

        val now = LocalDateTime.now()
        val signers = requests.map { req ->
            val sig = signaturesByRequestId[req.id]
            val effectiveStatus = when {
                req.status == "SIGNED" -> "SIGNED"
                req.status == "CANCELLED" -> "CANCELLED"
                req.expiresAt.isBefore(now) -> "EXPIRED"
                else -> "PENDING"
            }
            SignerDetailDto(
                email = req.signerEmail,
                status = effectiveStatus,
                signedAt = sig?.signedAt?.toString(),
                signatureToken = if (effectiveStatus == "PENDING") req.token else null,
                bundleToken = if (effectiveStatus == "PENDING") req.bundleToken else null,
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

    // ── 번들 상세 조회 (요청자용) ────────────────────────────────────────────
    @Transactional(readOnly = true)
    fun getBundleDetail(bundleId: Long, requesterEmail: String): BundleDetailResponse {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        val bundle = documentBundleRepository.findByIdAndOwner(bundleId, requester)
            ?: throw DocumentNotFoundException()

        val items = documentBundleItemRepository.findByBundle(bundle).sortedBy { it.ordinal }
        val documents = items.map { BundleDocumentInfoDto(it.ordinal, it.document.originalFilename, it.document.hashSha3256) }

        val allRequests = signatureRequestRepository.findByBundleIdOrderBySignerAndDocument(bundleId)
        val signaturesByRequestId = signatureRepository
            .findBySignatureRequestIn(allRequests)
            .associateBy { it.signatureRequest.id }

        val now = LocalDateTime.now()
        // 서명자별로 그룹화 (signer email → 요청 목록)
        val requestsBySignerEmail = allRequests.groupBy { it.signerEmail }

        val signerDetails = requestsBySignerEmail.map { (signerEmail, reqs) ->
            val reqsSorted = reqs.sortedBy { it.document.id }
            val first = reqsSorted.first()
            // 번들 전체 상태: 모두 SIGNED면 SIGNED, 하나라도 CANCELLED면 CANCELLED, 만료면 EXPIRED
            val effectiveStatus = when {
                reqsSorted.all { it.status == "SIGNED" } -> "SIGNED"
                reqsSorted.any { it.status == "CANCELLED" } -> "CANCELLED"
                reqsSorted.first().expiresAt.isBefore(now) -> "EXPIRED"
                else -> "PENDING"
            }
            val signedDocs = if (effectiveStatus == "SIGNED") {
                reqsSorted.mapIndexed { idx, req ->
                    BundleSignedDocDto(idx, req.document.originalFilename)
                }
            } else emptyList()

            BundleSignerDetailDto(
                email = signerEmail,
                status = effectiveStatus,
                signedAt = if (effectiveStatus == "SIGNED") signaturesByRequestId[first.id]?.signedAt?.toString() else null,
                bundleToken = if (effectiveStatus == "PENDING") first.bundleToken else null,
                signedDocuments = signedDocs,
            )
        }

        val first = allRequests.firstOrNull()
        val primaryDocName = items.firstOrNull()?.document?.originalFilename ?: ""

        return BundleDetailResponse(
            bundleId = bundleId,
            primaryDocName = primaryDocName,
            docCount = items.size,
            documents = documents,
            requesterEmail = requester.email,
            algorithm = "ML-DSA-65",
            requestedAt = first?.createdAt?.toString() ?: "",
            expiresAt = first?.expiresAt?.toString() ?: "",
            signers = signerDetails,
        )
    }

    // ── 요청자용 서명된 PDF 다운로드 (단건 문서 기준) ─────────────────────────
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

    // ── 번들 내 서명된 PDF 다운로드 (요청자용, index 지정) ──────────────────
    @Transactional(readOnly = true)
    fun getBundleSignedDocByRequester(bundleId: Long, signerEmail: String, docIndex: Int, requesterEmail: String): Pair<ByteArray, String> {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        documentBundleRepository.findByIdAndOwner(bundleId, requester)
            ?: throw DocumentNotFoundException()

        val requests = signatureRequestRepository
            .findByBundleTokenOrderByDocumentIdAsc(
                signatureRequestRepository.findByBundleIdOrderBySignerAndDocument(bundleId)
                    .firstOrNull { it.signerEmail.equals(signerEmail, ignoreCase = true) }
                    ?.bundleToken ?: throw SignatureRequestNotFoundException()
            )
            .filter { it.signerEmail.equals(signerEmail, ignoreCase = true) }

        val req = requests.getOrNull(docIndex) ?: throw SignatureRequestNotFoundException()
        if (req.status != "SIGNED") throw SignatureRequestNotFoundException()
        val signature = signatureRepository.findBySignatureRequest(req)
            ?: throw SignatureRequestNotFoundException()
        val bytes = storageService.download(signature.signedStorageKey)
        return Pair(bytes, req.document.originalFilename.addQusignedSuffix())
    }

    // ── 받은 문서 목록 (서명자용) ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    fun getReceivedDocuments(signerEmail: String): ReceivedDocumentsResponse {
        val requests = signatureRequestRepository.findBySignerEmailOrderByCreatedAtDesc(signerEmail)
        val now = LocalDateTime.now()
        val processedBundleTokens = mutableSetOf<String>()
        val items = mutableListOf<ReceivedDocumentItem>()

        for (req in requests) {
            val effectiveStatus = when {
                req.status == "SIGNED"    -> "SIGNED"
                req.status == "CANCELLED" -> "CANCELLED"
                req.expiresAt.isBefore(now) -> "EXPIRED"
                else -> "PENDING"
            }

            val bt = req.bundleToken
            if (bt != null) {
                if (bt in processedBundleTokens) continue
                processedBundleTokens.add(bt)
                val count = requests.count { it.bundleToken == bt }
                items.add(ReceivedDocumentItem(
                    requestId = req.id,
                    documentName = req.document.originalFilename,
                    requesterEmail = req.requester.email,
                    status = effectiveStatus,
                    token = req.token,
                    bundleToken = bt,
                    bundleDocCount = count,
                    expiresAt = req.expiresAt.toString(),
                    createdAt = req.createdAt?.toString() ?: "",
                    message = req.message,
                    isBundle = true,
                ))
            } else {
                items.add(ReceivedDocumentItem(
                    requestId = req.id,
                    documentName = req.document.originalFilename,
                    requesterEmail = req.requester.email,
                    status = effectiveStatus,
                    token = req.token,
                    bundleToken = null,
                    bundleDocCount = 1,
                    expiresAt = req.expiresAt.toString(),
                    createdAt = req.createdAt?.toString() ?: "",
                    message = req.message,
                    isBundle = false,
                ))
            }
        }
        return ReceivedDocumentsResponse(received = items)
    }

    // ── 취소 ────────────────────────────────────────────────────────────────
    @Transactional
    fun cancelSigner(documentId: Long, signerEmail: String, requesterEmail: String) {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        val document = documentRepository.findByIdAndUser(documentId, requester)
            ?: throw DocumentNotFoundException()
        val req = signatureRequestRepository.findByDocumentAndSignerEmail(document, signerEmail)
            ?: throw SignatureRequestNotFoundException()
        if (req.status != "PENDING") throw SignatureRequestNotCancellableException()
        req.status = "CANCELLED"

        userRepository.findByEmail(signerEmail)?.let { signer ->
            notificationService.createAndPublish(
                userId = signer.id,
                type = NotificationService.TYPE_SIGN_CANCELLED,
                title = "서명 요청 취소",
                message = "${requester.email}님이 '${document.originalFilename}' 서명 요청을 취소했습니다.",
                referenceId = req.id,
            )
        }
    }

    // ── 번들 서명자 취소 ─────────────────────────────────────────────────────
    @Transactional
    fun cancelBundleSigner(bundleId: Long, signerEmail: String, requesterEmail: String) {
        val requester = userRepository.findByEmail(requesterEmail) ?: throw DocumentNotFoundException()
        val bundle = documentBundleRepository.findByIdAndOwner(bundleId, requester)
            ?: throw DocumentNotFoundException()

        val requests = signatureRequestRepository.findByBundleIdOrderBySignerAndDocument(bundleId)
            .filter { it.signerEmail.equals(signerEmail, ignoreCase = true) }

        if (requests.isEmpty()) throw SignatureRequestNotFoundException()
        if (requests.any { it.status != "PENDING" }) throw SignatureRequestNotCancellableException()

        requests.forEach { it.status = "CANCELLED" }

        val items = documentBundleItemRepository.findByBundle(bundle).sortedBy { it.ordinal }
        val primaryDocName = items.firstOrNull()?.document?.originalFilename ?: ""
        val displayName = if (items.size > 1) "$primaryDocName 외 ${items.size - 1}건" else primaryDocName

        userRepository.findByEmail(signerEmail)?.let { signer ->
            notificationService.createAndPublish(
                userId = signer.id,
                type = NotificationService.TYPE_SIGN_CANCELLED,
                title = "서명 요청 취소",
                message = "${requester.email}님이 '$displayName' 서명 요청을 취소했습니다.",
                referenceId = bundleId,
            )
        }
    }

    // ── 검증 ────────────────────────────────────────────────────────────────
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

    // ── private helpers ──────────────────────────────────────────────────────
    private fun decryptAndSign(signer: User, password: String, document: com.qusign.document.entity.Document): ByteArray {
        val originalPdfBytes = storageService.download(document.storageKey)
        val documentHash = sha3256(originalPdfBytes)
        return signWithDecryptedKey(signer, password, documentHash)
    }

    private fun signDocument(storageKey: String, signatureBytes: ByteArray, signerEmail: String): ByteArray {
        val originalPdfBytes = storageService.download(storageKey)
        val documentHash = sha3256(originalPdfBytes)
        return pdfSignatureService.embedSignature(
            pdfBytes = originalPdfBytes,
            signature = signatureBytes,
            signerId = signerEmail,
            documentHash = documentHash,
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

    private fun sha3256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA3-256").digest(bytes)
}

private fun String.addQusignedSuffix(): String {
    val dot = lastIndexOf('.')
    return if (dot != -1) substring(0, dot) + "_qusigned" + substring(dot)
    else this + "_qusigned"
}
