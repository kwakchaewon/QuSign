package com.qusign.document.dto

import com.qusign.document.entity.Document

data class DocumentResponse(
    val id: Long,
    val originalFilename: String,
    val hashSha3256: String,
    val createdAt: String?,
    val signatureStatus: String = "NONE",
) {
    constructor(doc: Document, signatureStatus: String = "NONE") : this(
        id = doc.id,
        originalFilename = doc.originalFilename,
        hashSha3256 = doc.hashSha3256,
        createdAt = doc.createdAt?.toString(),
        signatureStatus = signatureStatus,
    )
}
