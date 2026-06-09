package com.qusign.signature.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.Base64

private const val KEY_SIGNATURE  = "QuSign-Signature"
private const val KEY_SIGNER_ID  = "QuSign-SignerId"
private const val KEY_SIGNED_AT  = "QuSign-SignedAt"
private const val KEY_DOC_HASH   = "QuSign-DocumentHash"
private const val KEY_SIGNER_IP  = "QuSign-SignerIP"

@Service
class PdfBoxSignatureService : PdfSignatureService {

    override fun embedSignature(
        pdfBytes: ByteArray,
        signature: ByteArray,
        signerId: String,
        documentHash: ByteArray,
        ipAddress: String,
    ): ByteArray {
        Loader.loadPDF(pdfBytes).use { doc ->
            val info = doc.documentInformation
            info.setCustomMetadataValue(KEY_SIGNATURE, Base64.getEncoder().encodeToString(signature))
            info.setCustomMetadataValue(KEY_SIGNER_ID, signerId)
            info.setCustomMetadataValue(KEY_SIGNED_AT, Instant.now().toString())
            info.setCustomMetadataValue(KEY_DOC_HASH, Base64.getEncoder().encodeToString(documentHash))
            info.setCustomMetadataValue(KEY_SIGNER_IP, ipAddress)

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
            val hashB64  = info.getCustomMetadataValue(KEY_DOC_HASH)   ?: return null
            return SignatureMetadata(
                signature    = Base64.getDecoder().decode(b64),
                signerId     = signerId,
                signedAt     = signedAt,
                documentHash = Base64.getDecoder().decode(hashB64),
            )
        }
    }
}
