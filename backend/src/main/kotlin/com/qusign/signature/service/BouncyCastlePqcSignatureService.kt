package com.qusign.signature.service

import org.bouncycastle.jcajce.spec.MLDSAParameterSpec
import org.springframework.stereotype.Service
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

// ML-DSA-65: NIST Level 3 (보안·성능 균형)
// 공개키 1952 bytes, 개인키 4032 bytes, 서명값 3309 bytes
@Service
class BouncyCastlePqcSignatureService : PqcSignatureService {

    override fun generateKeyPair(): KeyPair {
        val kpg = KeyPairGenerator.getInstance("ML-DSA", "BC")
        kpg.initialize(MLDSAParameterSpec.ml_dsa_65)
        return kpg.generateKeyPair()
    }

    override fun sign(privateKey: PrivateKey, message: ByteArray): ByteArray {
        val signer = Signature.getInstance("ML-DSA", "BC")
        signer.initSign(privateKey)
        signer.update(message)
        return signer.sign()
    }

    override fun verify(publicKey: PublicKey, message: ByteArray, signature: ByteArray): Boolean {
        val verifier = Signature.getInstance("ML-DSA", "BC")
        verifier.initVerify(publicKey)
        verifier.update(message)
        return verifier.verify(signature)
    }
}
