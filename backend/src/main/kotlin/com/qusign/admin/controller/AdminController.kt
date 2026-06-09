package com.qusign.admin.controller

import com.qusign.admin.dto.AdminStatsResponse
import com.qusign.admin.dto.AdminUserResponse
import com.qusign.admin.dto.PagedResponse
import com.qusign.admin.service.AdminService
import com.qusign.audit.dto.AuditLogResponse
import com.qusign.audit.entity.AuditEventType
import com.qusign.common.response.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(private val adminService: AdminService) {

    @GetMapping("/stats")
    fun getStats(): ApiResponse<AdminStatsResponse> =
        ApiResponse.ok(adminService.getStats())

    @GetMapping("/users")
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) email: String?,
    ): ApiResponse<PagedResponse<AdminUserResponse>> =
        ApiResponse.ok(adminService.getUsers(page, size, email))

    @PutMapping("/users/{email}/disable")
    fun disableUser(@PathVariable email: String): ApiResponse<Unit> {
        adminService.disableUser(email)
        return ApiResponse.ok("사용자가 비활성화되었습니다")
    }

    @GetMapping("/audit")
    fun getAuditLogs(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(required = false) eventType: AuditEventType?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime?,
    ): ApiResponse<PagedResponse<AuditLogResponse>> =
        ApiResponse.ok(adminService.getAuditLogs(page, size, eventType, startDate, endDate))

    @GetMapping("/users/{email}/audit")
    fun getUserAuditLogs(
        @PathVariable email: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ): ApiResponse<PagedResponse<AuditLogResponse>> =
        ApiResponse.ok(adminService.getUserAuditLogs(email, page, size))

    @GetMapping("/audit/export")
    fun exportAuditLogs(): ApiResponse<List<AuditLogResponse>> =
        ApiResponse.ok(adminService.exportAuditLogs())
}
