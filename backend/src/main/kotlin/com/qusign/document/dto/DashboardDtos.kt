package com.qusign.document.dto

data class DashboardResponse(
    val totalDocuments: Int,
    val signedCount: Int,
    val pendingCount: Int,
    val expiredCount: Int,
    val recentRequests: List<RecentRequestItem>,
)

data class RecentRequestItem(
    val id: Long,
    val documentName: String,
    val signers: List<String>,
    val createdAt: String,
    val status: String,
)

data class DashboardSummaryResponse(
    val sentPending: Int,
    val sentSigned: Int,
    val receivedUnsigned: Int,
)

data class DashboardActionItem(
    val direction: String,       // "SENT" | "RECEIVED"
    val docName: String,
    val counterpart: String,     // RECEIVED: requesterEmail, SENT: signerEmail
    val createdAt: String,
    val signPath: String,
    val token: String? = null,
    val bundleToken: String? = null,
    val bundleId: Long? = null,
)

data class DashboardActionItemsResponse(
    val items: List<DashboardActionItem>,
)
