package com.qusign.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class StorageConfig(
    @Value("\${storage.endpoint:}") private val endpoint: String,
    @Value("\${storage.region}") private val region: String,
    @Value("\${storage.access-key:}") private val accessKey: String,
    @Value("\${storage.secret-key:}") private val secretKey: String,
) {
    @Bean
    fun s3Client(): S3Client {
        val builder = S3Client.builder().region(Region.of(region))

        return if (endpoint.isBlank()) {
            // AWS 네이티브 모드: EC2 IAM 역할 자동 인증
            builder
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()
        } else {
            // MinIO 모드: 정적 자격증명 + 엔드포인트 오버라이드
            builder
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                )
                .serviceConfiguration { it.pathStyleAccessEnabled(true) }
                .build()
        }
    }
}
