package com.qusign.common.storage

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

@Service
class S3StorageService(
    private val s3Client: S3Client,
    @Value("\${storage.bucket}") private val bucket: String,
) : StorageService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun ensureBucketExists() {
        try {
            val exists = s3Client.listBuckets().buckets().any { it.name() == bucket }
            if (!exists) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
                log.info("버킷 생성 완료: $bucket")
            }
        } catch (e: Exception) {
            log.warn("버킷 초기화 실패 (MinIO 미실행?): ${e.message}")
        }
    }

    override fun upload(key: String, data: ByteArray, contentType: String) {
        s3Client.putObject(
            PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
            RequestBody.fromBytes(data),
        )
    }

    override fun download(key: String): ByteArray =
        s3Client.getObjectAsBytes(
            GetObjectRequest.builder().bucket(bucket).key(key).build()
        ).asByteArray()
}
