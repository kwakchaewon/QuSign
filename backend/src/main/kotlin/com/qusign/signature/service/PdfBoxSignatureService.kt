package com.qusign.signature.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.Base64

// PDF 문서 정보 사전(Document Information Dictionary)의 커스텀 필드로 서명값을 저장한다.
// 키 접두사 "QuSign-"으로 충돌을 방지한다.
private const val KEY_SIGNATURE  = "QuSign-Signature"
private const val KEY_SIGNER_ID  = "QuSign-SignerId"
private const val KEY_SIGNED_AT  = "QuSign-SignedAt"

@Service
class PdfBoxSignatureService : PdfSignatureService {

    override fun embedSignature(pdfBytes: ByteArray, signature: ByteArray, signerId: String): ByteArray {
        Loader.loadPDF(pdfBytes).use { doc ->
            val info = doc.documentInformation
            info.setCustomMetadataValue(KEY_SIGNATURE, Base64.getEncoder().encodeToString(signature))
            info.setCustomMetadataValue(KEY_SIGNER_ID, signerId)
            info.setCustomMetadataValue(KEY_SIGNED_AT, Instant.now().toString())

            return ByteArrayOutputStream().also { doc.save(it) }.toByteArray()
        }
    }

    override fun extractSignature(pdfBytes: ByteArray): ByteArray? {
        Loader.loadPDF(pdfBytes).use { doc ->
            val b64 = doc.documentInformation.getCustomMetadataValue(KEY_SIGNATURE) ?: return null
            return Base64.getDecoder().decode(b64)
        }
    }

    override fun extractMetadata(pdfBytes: ByteArray): SignatureMetadata? {
        Loader.loadPDF(pdfBytes).use { doc ->
            val info = doc.documentInformation
            val b64      = info.getCustomMetadataValue(KEY_SIGNATURE)  ?: return null
            val signerId = info.getCustomMetadataValue(KEY_SIGNER_ID)  ?: return null
            val signedAt = info.getCustomMetadataValue(KEY_SIGNED_AT)  ?: return null
            return SignatureMetadata(
                signature = Base64.getDecoder().decode(b64),
                signerId  = signerId,
                signedAt  = signedAt,
            )
        }
    }
}
