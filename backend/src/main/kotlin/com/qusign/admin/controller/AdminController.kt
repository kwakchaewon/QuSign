package com.qusign.admin.controller

import com.qusign.admin.dto.AdminStatsResponse
import com.qusign.admin.dto.AdminUserResponse
import com.qusign.admin.dto.PagedResponse
import com.qusign.admin.service.AdminService
import com.qusign.audit.dto.AuditLogResponse
import com.qusign.audit.entity.AuditEventType
import com.qusign.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Tag(name = "Admin", description = "관리자 전용 — ADMIN 롤 필요")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(private val adminService: AdminService) {

    @Operation(summary = "전체 통계 조회", description = "가입자 수, 서명 요청 현황(대기/완료/취소)을 반환합니다.")
    @GetMapping("/stats")
    fun getStats(): ApiResponse<AdminStatsResponse> =
        ApiResponse.ok(adminService.getStats())

    @Operation(summary = "사용자 목록 조회", description = "전체 사용자를 페이지 단위로 조회합니다. 이메일 키워드 검색 가능.")
    @GetMapping("/users")
    fun getUsers(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "이메일 검색 키워드 (선택)")
        @RequestParam(required = false) email: String?,
    ): ApiResponse<PagedResponse<AdminUserResponse>> =
        ApiResponse.ok(adminService.getUsers(page, size, email))

    @Operation(summary = "사용자 비활성화", description = "특정 사용자를 비활성화합니다 (disabledAt 설정). 로그인 불가 상태가 됩니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "비활성화 성공"),
        SwaggerApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
    )
    @PutMapping("/users/{email}/disable")
    fun disableUser(
        @Parameter(description = "비활성화할 사용자 이메일", required = true)
        @PathVariable email: String,
    ): ApiResponse<Unit> {
        adminService.disableUser(email)
        return ApiResponse.ok("사용자가 비활성화되었습니다")
    }

    @Operation(
        summary = "전체 감사 로그 조회",
        description = "이벤트 타입, 날짜 범위로 필터링 가능한 감사 로그 목록을 반환합니다."
    )
    @GetMapping("/audit")
    fun getAuditLogs(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "50")
        @RequestParam(defaultValue = "50") size: Int,
        @Parameter(
            description = "이벤트 타입 필터 (선택). 가능한 값: SIGN_REQUEST_CREATED, BUNDLE_REQUEST_CREATED, SIGNED, BUNDLE_SIGNED, SIGNER_CANCELLED, BUNDLE_SIGNER_CANCELLED, SIGNED_DOCUMENT_DOWNLOADED, TIMESTAMP_OBTAINED"
        )
        @RequestParam(required = false) eventType: AuditEventType?,
        @Parameter(description = "시작 일시 (ISO 8601, 예: 2025-01-01T00:00:00)", example = "2025-01-01T00:00:00")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @Parameter(description = "종료 일시 (ISO 8601, 예: 2025-12-31T23:59:59)", example = "2025-12-31T23:59:59")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?,
    ): ApiResponse<PagedResponse<AuditLogResponse>> =
        ApiResponse.ok(adminService.getAuditLogs(page, size, eventType, startDate, endDate))

    @Operation(summary = "사용자별 감사 로그 조회", description = "특정 사용자의 감사 로그를 페이지 단위로 반환합니다.")
    @GetMapping("/users/{email}/audit")
    fun getUserAuditLogs(
        @Parameter(description = "조회할 사용자 이메일", required = true)
        @PathVariable email: String,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "50")
        @RequestParam(defaultValue = "50") size: Int,
    ): ApiResponse<PagedResponse<AuditLogResponse>> =
        ApiResponse.ok(adminService.getUserAuditLogs(email, page, size))

    @Operation(summary = "전체 감사 로그 내보내기", description = "전체 감사 로그를 단일 배열로 반환합니다. 데이터 건수가 많을 수 있습니다.")
    @GetMapping("/audit/export")
    fun exportAuditLogs(): ApiResponse<List<AuditLogResponse>> =
        ApiResponse.ok(adminService.exportAuditLogs())
}
