package com.qusign.document.controller

import com.qusign.common.response.ApiResponse
import com.qusign.document.dto.DashboardActionItemsResponse
import com.qusign.document.dto.DashboardResponse
import com.qusign.document.dto.DashboardSummaryResponse
import com.qusign.document.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Dashboard", description = "대시보드 통계 · 액션 아이템")
@RestController
@RequestMapping("/api/dashboard")
class DashboardController(private val dashboardService: DashboardService) {

    @Operation(
        summary = "전체 대시보드 조회",
        description = "총 문서 수, 서명 완료/대기/만료 건수, 최근 서명 요청 목록을 반환합니다."
    )
    @GetMapping
    fun getDashboard(
        @AuthenticationPrincipal email: String,
    ): ApiResponse<DashboardResponse> = ApiResponse.ok(dashboardService.getDashboard(email))

    @Operation(
        summary = "요약 통계 조회",
        description = "내가 보낸 서명 요청 중 대기/완료 건수, 내가 받아서 미서명인 건수를 반환합니다."
    )
    @GetMapping("/summary")
    fun getSummary(
        @AuthenticationPrincipal email: String,
    ): ApiResponse<DashboardSummaryResponse> = ApiResponse.ok(dashboardService.getSummary(email))

    @Operation(
        summary = "액션 아이템 조회",
        description = "즉시 처리가 필요한 항목(내가 보낸 것 중 대기, 내가 받아서 미서명)을 반환합니다."
    )
    @GetMapping("/action-items")
    fun getActionItems(
        @AuthenticationPrincipal email: String,
    ): ApiResponse<DashboardActionItemsResponse> = ApiResponse.ok(dashboardService.getActionItems(email))
}
