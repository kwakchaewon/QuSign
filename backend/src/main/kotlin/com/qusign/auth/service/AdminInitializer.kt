package com.qusign.auth.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class AdminInitializer(
    private val authService: AuthService,
    @Value("\${admin.email:}") private val adminEmail: String,
    @Value("\${admin.password:}") private val adminPassword: String,
) {
    private val log = LoggerFactory.getLogger(AdminInitializer::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        if (adminEmail.isBlank() || adminPassword.isBlank()) return
        authService.ensureAdmin(adminEmail, adminPassword)
    }
}
