package com.qusign.document.service

import com.qusign.auth.repository.UserRepository
import com.qusign.document.dto.DashboardActionItem
import com.qusign.document.dto.DashboardActionItemsResponse
import com.qusign.document.dto.DashboardResponse
import com.qusign.document.dto.DashboardSummaryResponse
import com.qusign.document.dto.RecentRequestItem
import com.qusign.document.repository.DocumentBundleItemRepository
import com.qusign.document.repository.DocumentRepository
import com.qusign.signature.entity.SignatureRequest
import com.qusign.signature.repository.SignatureRequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class DashboardService(
    private val userRepository: UserRepository,
    private val documentRepository: DocumentRepository,
    private val signatureRequestRepository: SignatureRequestRepository,
    private val documentBundleItemRepository: DocumentBundleItemRepository,
) {
    fun getDashboard(email: String): DashboardResponse {
        val user = userRepository.findByEmail(email)
            ?: return DashboardResponse(0, 0, 0, 0, emptyList())
        val documents = documentRepository.findByUserOrderByCreatedAtDesc(user)
        if (documents.isEmpty()) return DashboardResponse(0, 0, 0, 0, emptyList())

        val requests = signatureRequestRepository.findByDocumentIn(documents)
        val now = LocalDateTime.now()
        val requestsByDocId = requests.groupBy { it.document.id }

        var signedCount = 0
        var pendingCount = 0
        var expiredCount = 0
        documents.forEach { doc ->
            when (aggregateStatus(requestsByDocId[doc.id].orEmpty(), now)) {
                "SIGNED"  -> signedCount++
                "PENDING" -> pendingCount++
                "EXPIRED" -> expiredCount++
            }
        }

        val recentRequests = documents
            .filter { requestsByDocId.containsKey(it.id) }
            .sortedByDescending { doc ->
                requestsByDocId[doc.id]!!.maxOf { it.createdAt ?: LocalDateTime.MIN }
            }
            .take(5)
            .map { doc ->
                val docRequests = requestsByDocId[doc.id]!!
                RecentRequestItem(
                    id = doc.id,
                    documentName = doc.originalFilename,
                    signers = docRequests.map { it.signerEmail }.distinct(),
                    createdAt = (docRequests.maxOfOrNull { it.createdAt ?: LocalDateTime.MIN }
                        ?: LocalDateTime.MIN).toString(),
                    status = aggregateStatus(docRequests, now),
                )
            }

        return DashboardResponse(
            totalDocuments = documents.size,
            signedCount = signedCount,
            pendingCount = pendingCount,
            expiredCount = expiredCount,
            recentRequests = recentRequests,
        )
    }

    fun getSummary(email: String): DashboardSummaryResponse {
        val user = userRepository.findByEmail(email)
            ?: return DashboardSummaryResponse(0, 0, 0)
        val now = LocalDateTime.now()

        val documents = documentRepository.findByUserOrderByCreatedAtDesc(user)
        val requests = if (documents.isEmpty()) emptyList()
                       else signatureRequestRepository.findByDocumentIn(documents)
        val requestsByDocId = requests.groupBy { it.document.id }

        var sentPending = 0
        var sentSigned = 0
        documents.forEach { doc ->
            when (aggregateStatus(requestsByDocId[doc.id].orEmpty(), now)) {
                "SIGNED"  -> sentSigned++
                "PENDING" -> sentPending++
            }
        }

        val receivedUnsigned = signatureRequestRepository
            .findBySignerEmailOrderByCreatedAtDesc(email)
            .count { it.status == "PENDING" && it.expiresAt.isAfter(now) }

        return DashboardSummaryResponse(
            sentPending = sentPending,
            sentSigned = sentSigned,
            receivedUnsigned = receivedUnsigned,
        )
    }

    fun getActionItems(email: String): DashboardActionItemsResponse {
        val user = userRepository.findByEmail(email)
            ?: return DashboardActionItemsResponse(emptyList())
        val now = LocalDateTime.now()
        val items = mutableListOf<DashboardActionItem>()

        // 1. 받은 것 중 내가 서명해야 할 항목
        val receivedPending = signatureRequestRepository
            .findBySignerEmailOrderByCreatedAtDesc(email)
            .filter { it.status == "PENDING" && it.expiresAt.isAfter(now) }

        val seenBundleTokens = mutableSetOf<String>()
        receivedPending.forEach { req ->
            val bt = req.bundleToken
            if (bt != null) {
                if (seenBundleTokens.add(bt)) {
                    items.add(DashboardActionItem(
                        direction = "RECEIVED",
                        docName = req.document.originalFilename,
                        counterpart = req.requester.email,
                        createdAt = (req.createdAt ?: now).toString(),
                        signPath = "/sign/$bt",
                        bundleToken = bt,
                    ))
                }
            } else {
                items.add(DashboardActionItem(
                    direction = "RECEIVED",
                    docName = req.document.originalFilename,
                    counterpart = req.requester.email,
                    createdAt = (req.createdAt ?: now).toString(),
                    signPath = "/sign/${req.token}",
                    token = req.token,
                ))
            }
        }

        // 2. 내가 보낸 것 중 상대방 서명 대기 중인 항목
        val documents = documentRepository.findByUserOrderByCreatedAtDesc(user)
        if (documents.isNotEmpty()) {
            val allRequests = signatureRequestRepository.findByDocumentIn(documents)
            val requestsByDocId = allRequests.groupBy { it.document.id }
            val bundleItems = documentBundleItemRepository.findByDocumentIn(documents)
            val bundleByDocId = bundleItems.associateBy { it.document.id }

            documents.forEach { doc ->
                val docRequests = requestsByDocId[doc.id].orEmpty()
                if (aggregateStatus(docRequests, now) != "PENDING") return@forEach

                val bundleItem = bundleByDocId[doc.id]
                // 번들 비-primary 문서 제외
                if (bundleItem != null && bundleItem.ordinal != 0) return@forEach

                val firstPendingSigner = docRequests
                    .filter { it.status == "PENDING" && it.expiresAt.isAfter(now) }
                    .firstOrNull()?.signerEmail ?: return@forEach

                val signPath = if (bundleItem != null) "/bundles/${bundleItem.bundle.id}"
                               else "/documents/${doc.id}"

                items.add(DashboardActionItem(
                    direction = "SENT",
                    docName = doc.originalFilename,
                    counterpart = firstPendingSigner,
                    createdAt = (docRequests.maxOfOrNull { it.createdAt ?: LocalDateTime.MIN } ?: now).toString(),
                    signPath = signPath,
                    bundleId = bundleItem?.bundle?.id,
                ))
            }
        }

        items.sortByDescending { it.createdAt }
        return DashboardActionItemsResponse(items)
    }

    private fun aggregateStatus(requests: List<SignatureRequest>, now: LocalDateTime): String {
        if (requests.isEmpty()) return "NONE"
        if (requests.all { it.status == "SIGNED" }) return "SIGNED"
        if (requests.any { it.status == "PENDING" && it.expiresAt.isAfter(now) }) return "PENDING"
        return "EXPIRED"
    }
}
