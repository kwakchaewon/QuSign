package com.qusign.signature.service

import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.cms.ContentInfo
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampRequestGenerator
import org.bouncycastle.tsp.TimeStampResponse
import org.bouncycastle.tsp.TimeStampToken
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * RFC 3161 Time-Stamp Protocol 클라이언트.
 *
 * TIMESTAMP_TSA_URL 환경변수가 설정된 경우에만 활성화된다.
 * TSA 장애 시에도 서명 작업이 실패하지 않도록 호출부에서 예외를 catch해야 한다.
 *
 * KISA TSA 예: http://tsa.kisa.or.kr/TSA
 * 테스트용 공개 TSA: https://freetsa.org/tsr
 */
@Service
class RfcTimestampService(
    @Value("\${timestamp.tsa-url:}") private val tsaUrl: String,
    @Value("\${timestamp.connect-timeout-ms:5000}") private val connectTimeoutMs: Int,
    @Value("\${timestamp.read-timeout-ms:10000}") private val readTimeoutMs: Int,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    val enabled: Boolean get() = tsaUrl.isNotBlank()

    /**
     * [contentHash]에 대한 RFC 3161 타임스탬프를 TSA에 요청한다.
     *
     * @param contentHash SHA-256(서명된 PDF 바이트) — 32 bytes
     * @return TSA 응답 메타데이터와 DER 인코딩된 TimeStampToken
     * @throws IllegalStateException TSA URL이 설정되지 않은 경우
     * @throws RuntimeException TSA 통신 오류 또는 거부 응답
     */
    fun requestTimestamp(contentHash: ByteArray): TsaResult {
        check(enabled) { "TSA URL이 설정되지 않았습니다 (timestamp.tsa-url)" }

        val nonce = BigInteger(64, SecureRandom())
        val request = TimeStampRequestGenerator()
            .apply { setCertReq(true) }
            .generate(TSPAlgorithms.SHA256, contentHash, nonce)

        log.debug("RFC 3161 타임스탬프 요청 → {}", tsaUrl)

        val responseBytes = postRequest(request.encoded)

        val response = TimeStampResponse(responseBytes)
        response.validate(request)

        val statusCode = response.status
        if (statusCode != 0 && statusCode != 1) {
            throw RuntimeException("TSA 거부 응답: status=$statusCode, info=${response.statusString}")
        }

        val token = response.timeStampToken
            ?: throw RuntimeException("TSA 응답에 TimeStampToken이 없습니다")
        val info = token.timeStampInfo

        val result = TsaResult(
            tokenDer = token.encoded,
            tsaUrl = tsaUrl,
            serialNumber = info.serialNumber.toString(),
            genTime = info.genTime.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime(),
            messageImprint = info.messageImprintDigest,
        )
        log.info("RFC 3161 타임스탬프 획득: serial={}, genTime={}, tsa={}", result.serialNumber, result.genTime, tsaUrl)
        return result
    }

    /**
     * DER 인코딩된 토큰의 messageImprint가 [expectedHash]와 일치하는지 확인한다.
     * TSA 인증서 체인 검증은 포함하지 않는다 (네트워크 불필요).
     */
    fun verifyToken(tokenDer: ByteArray, expectedHash: ByteArray): Boolean {
        return try {
            val content = ContentInfo.getInstance(ASN1Primitive.fromByteArray(tokenDer))
            val token = TimeStampToken(content)
            token.timeStampInfo.messageImprintDigest.contentEquals(expectedHash)
        } catch (e: Exception) {
            log.warn("타임스탬프 토큰 검증 실패", e)
            false
        }
    }

    private fun postRequest(requestBytes: ByteArray): ByteArray {
        val conn = URL(tsaUrl).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/timestamp-query")
            conn.setRequestProperty("Accept", "application/timestamp-reply")
            conn.connectTimeout = connectTimeoutMs
            conn.readTimeout = readTimeoutMs
            conn.doOutput = true
            conn.outputStream.use { it.write(requestBytes) }

            val code = conn.responseCode
            if (code != 200) throw RuntimeException("TSA HTTP $code")
            return conn.inputStream.readBytes()
        } finally {
            conn.disconnect()
        }
    }
}

data class TsaResult(
    val tokenDer: ByteArray,
    val tsaUrl: String,
    val serialNumber: String,
    /** TSA 공인 시각 (UTC) */
    val genTime: LocalDateTime,
    /** TSA가 서명한 해시 (SHA-256, 32 bytes) */
    val messageImprint: ByteArray,
)
