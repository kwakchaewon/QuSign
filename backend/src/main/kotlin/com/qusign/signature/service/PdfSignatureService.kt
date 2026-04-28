package com.qusign.signature.service

interface PdfSignatureService {
    fun embedSignature(
        pdfBytes: ByteArray,
        signature: ByteArray,
        signerId: String,
        documentHash: ByteArray,
    ): ByteArray

    fun extractSignature(pdfBytes: ByteArray): ByteArray?

    fun extractMetadata(pdfBytes: ByteArray): SignatureMetadata?
}

data class SignatureMetadata(
    val signature: ByteArray,
    val signerId: String,
    val signedAt: String,
    val documentHash: ByteArray,
)
