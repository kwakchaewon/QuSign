package com.qusign.signature.service

interface PdfSignatureService {
    /**
     * PDF 바이트에 ML-DSA 서명값(Base64)과 서명 메타데이터를 삽입한다.
     * @param pdfBytes   원본 PDF
     * @param signature  ML-DSA 서명값 (raw bytes)
     * @param signerId   서명자 식별자 (email or user ID)
     * @return 서명값이 삽입된 PDF 바이트
     */
    fun embedSignature(pdfBytes: ByteArray, signature: ByteArray, signerId: String): ByteArray

    /**
     * PDF에서 ML-DSA 서명값을 추출한다.
     * @return 서명값 raw bytes, 없으면 null
     */
    fun extractSignature(pdfBytes: ByteArray): ByteArray?

    /**
     * PDF에서 서명 메타데이터 전체를 추출한다.
     */
    fun extractMetadata(pdfBytes: ByteArray): SignatureMetadata?
}

data class SignatureMetadata(
    val signature: ByteArray,
    val signerId: String,
    val signedAt: String,
)
