package com.qusign.signature.service

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

interface PqcSignatureService {
    fun generateKeyPair(): KeyPair
    fun sign(privateKey: PrivateKey, message: ByteArray): ByteArray
    fun verify(publicKey: PublicKey, message: ByteArray, signature: ByteArray): Boolean
}
