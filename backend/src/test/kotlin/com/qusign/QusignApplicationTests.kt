package com.qusign

import com.qusign.common.email.EmailService
import com.qusign.common.storage.StorageService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
class QusignApplicationTests {

    @MockitoBean lateinit var storageService: StorageService
    @MockitoBean lateinit var emailService: EmailService
    @MockitoBean lateinit var redisMessageListenerContainer: RedisMessageListenerContainer

    @Test
    fun contextLoads() {
    }
}
