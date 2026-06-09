package com.qusign.signature.service

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.bouncycastle.tsp.TSPAlgorithms
import org.bouncycastle.tsp.TimeStampTokenGenerator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Date

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RfcTimestampServiceTest {

    private lateinit var tokenDer: ByteArray
    private lateinit var contentHash: ByteArray

    /**
     * BouncyCastle로 자체 서명된 테스트용 TimeStampToken을 생성한다.
     * 실제 TSA와 통신하지 않으므로 네트워크 불필요.
     */
    @BeforeAll
    fun setUp() {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048, SecureRandom())
        val kp = kpg.generateKeyPair()

        val subject = X500Name("CN=Test TSA")
        val notBefore = Date(System.currentTimeMillis() - 10_000)
        val notAfter = Date(System.currentTimeMillis() + 3_600_000)

        val certBuilder = JcaX509v3CertificateBuilder(
            subject, BigInteger.ONE, notBefore, notAfter, subject, kp.public
        ).addExtension(
            Extension.extendedKeyUsage,
            true,
            ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping),
        )
        val signer = JcaContentSignerBuilder("SHA256WithRSA").build(kp.private)
        val cert = JcaX509CertificateConverter().getCertificate(certBuilder.build(signer))

        val signerInfoGen = JcaSimpleSignerInfoGeneratorBuilder()
            .build("SHA256WithRSA", kp.private, cert)
        val sha256Calc = JcaDigestCalculatorProviderBuilder().build()
            .get(AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256))
        val tokenGen = TimeStampTokenGenerator(
            signerInfoGen,
            sha256Calc,
            ASN1ObjectIdentifier("1.2.3.4.5.6.7"),
        ).apply {
            addCertificates(JcaCertStore(listOf(cert)))
        }

        contentHash = MessageDigest.getInstance("SHA-256").digest("hello QuSign".toByteArray())
        val request = org.bouncycastle.tsp.TimeStampRequestGenerator()
            .apply { setCertReq(true) }
            .generate(TSPAlgorithms.SHA256, contentHash, BigInteger.TEN)

        tokenDer = tokenGen.generate(request, BigInteger.ONE, Date()).encoded
    }

    @Test
    fun `enabled is false when tsaUrl is blank`() {
        val service = RfcTimestampService(tsaUrl = "", connectTimeoutMs = 5000, readTimeoutMs = 10000)
        assertFalse(service.enabled)
    }

    @Test
    fun `enabled is true when tsaUrl is configured`() {
        val service = RfcTimestampService(tsaUrl = "https://freetsa.org/tsr", connectTimeoutMs = 5000, readTimeoutMs = 10000)
        assertTrue(service.enabled)
    }

    @Test
    fun `requestTimestamp throws when not enabled`() {
        val service = RfcTimestampService(tsaUrl = "", connectTimeoutMs = 5000, readTimeoutMs = 10000)
        assertThrows(IllegalStateException::class.java) {
            service.requestTimestamp(ByteArray(32))
        }
    }

    @Test
    fun `verifyToken returns true for matching hash`() {
        val service = RfcTimestampService(tsaUrl = "", connectTimeoutMs = 5000, readTimeoutMs = 10000)
        assertTrue(service.verifyToken(tokenDer, contentHash))
    }

    @Test
    fun `verifyToken returns false for mismatched hash`() {
        val service = RfcTimestampService(tsaUrl = "", connectTimeoutMs = 5000, readTimeoutMs = 10000)
        val wrongHash = MessageDigest.getInstance("SHA-256").digest("wrong content".toByteArray())
        assertFalse(service.verifyToken(tokenDer, wrongHash))
    }

    @Test
    fun `verifyToken returns false for garbage bytes`() {
        val service = RfcTimestampService(tsaUrl = "", connectTimeoutMs = 5000, readTimeoutMs = 10000)
        assertFalse(service.verifyToken(ByteArray(64) { it.toByte() }, contentHash))
    }
}
