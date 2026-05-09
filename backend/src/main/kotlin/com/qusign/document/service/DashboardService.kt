package com.qusign.document.service

import com.qusign.auth.repository.UserRepository
import com.qusign.document.dto.DashboardResponse
import com.qusign.document.dto.RecentRequestItem
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

    private fun aggregateStatus(requests: List<SignatureRequest>, now: LocalDateTime): String {
        if (requests.isEmpty()) return "NONE"
        if (requests.all { it.status == "SIGNED" }) return "SIGNED"
        if (requests.any { it.status == "PENDING" && it.expiresAt.isAfter(now) }) return "PENDING"
        return "EXPIRED"
    }
}
