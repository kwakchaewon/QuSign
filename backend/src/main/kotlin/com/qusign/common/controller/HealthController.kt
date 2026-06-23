package com.qusign.common.controller

import com.qusign.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Health", description = "서비스 헬스 체크")
@RestController
@RequestMapping("/api")
class HealthController {

    @Operation(summary = "헬스 체크", description = "서비스 상태를 확인합니다. 인증 불필요.")
    @GetMapping("/health")
    fun health(): ApiResponse<Map<String, String>> =
        ApiResponse.ok(mapOf("status" to "UP", "service" to "QuSign"))
}
