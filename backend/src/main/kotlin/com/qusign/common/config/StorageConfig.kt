package com.qusign.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class StorageConfig(
    @Value("\${storage.endpoint}") private val endpoint: String,
    @Value("\${storage.access-key}") private val accessKey: String,
    @Value("\${storage.secret-key}") private val secretKey: String,
) {
    @Bean
    fun s3Client(): S3Client = S3Client.builder()
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
        )
        .region(Region.US_EAST_1)
        .serviceConfiguration { it.pathStyleAccessEnabled(true) }
        .build()
}
