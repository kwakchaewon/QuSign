package com.qusign.common.controller

import com.qusign.common.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HealthController {

    @GetMapping("/health")
    fun health(): ApiResponse<Map<String, String>> =
        ApiResponse.ok(mapOf("status" to "UP", "service" to "QuSign"))
}
