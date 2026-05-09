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
