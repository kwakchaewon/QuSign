package com.qusign.document.controller

import com.qusign.common.response.ApiResponse
import com.qusign.document.dto.DashboardResponse
import com.qusign.document.service.DashboardService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(private val dashboardService: DashboardService) {

    @GetMapping
    fun getDashboard(
        @AuthenticationPrincipal email: String,
    ): ApiResponse<DashboardResponse> = ApiResponse.ok(dashboardService.getDashboard(email))
}
