package com.qusign.common.storage

interface StorageService {
    fun upload(key: String, data: ByteArray, contentType: String)
    fun download(key: String): ByteArray
}
