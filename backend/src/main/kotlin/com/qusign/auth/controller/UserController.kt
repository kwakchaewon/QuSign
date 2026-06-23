package com.qusign.auth.controller

import com.qusign.auth.dto.ChangePasswordRequest
import com.qusign.auth.dto.NotificationSettingsResponse
import com.qusign.auth.dto.UpdateNotificationSettingsRequest
import com.qusign.auth.dto.UserProfileResponse
import com.qusign.auth.service.UserService
import com.qusign.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "User", description = "사용자 프로필 · 비밀번호 · 알림 설정")
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 이메일과 알림 설정을 반환합니다.")
    @GetMapping("/me")
    fun getProfile(@AuthenticationPrincipal email: String): ApiResponse<UserProfileResponse> =
        ApiResponse.ok(userService.getProfile(email))

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다. PQC 개인키 재암호화가 함께 수행됩니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        SwaggerApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 유효성 오류"),
    )
    @PutMapping("/password")
    fun changePassword(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ApiResponse<Unit> {
        userService.changePassword(email, request.currentPassword, request.newPassword)
        return ApiResponse.ok("비밀번호가 변경되었습니다")
    }

    @Operation(summary = "알림 설정 변경", description = "서명 요청 · 완료 · 주간 리포트 · 마케팅 알림 수신 여부를 업데이트합니다.")
    @PutMapping("/notification-settings")
    fun updateNotificationSettings(
        @AuthenticationPrincipal email: String,
        @RequestBody request: UpdateNotificationSettingsRequest,
    ): ApiResponse<NotificationSettingsResponse> =
        ApiResponse.ok(userService.updateNotificationSettings(email, request))

    @Operation(summary = "사용자 검색", description = "이메일 키워드로 사용자를 검색합니다. 최소 2글자 이상 입력해야 하며 최대 8명을 반환합니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "검색 결과 (이메일 목록)"),
        SwaggerApiResponse(responseCode = "400", description = "검색어가 2글자 미만"),
    )
    @GetMapping("/search")
    fun searchUsers(
        @AuthenticationPrincipal email: String,
        @Parameter(description = "검색 키워드 (최소 2글자)", required = true, example = "hong")
        @RequestParam q: String,
    ): ApiResponse<List<String>> = ApiResponse.ok(userService.searchUsers(email, q))

    @Operation(summary = "계정 삭제", description = "로그인한 계정을 소프트 삭제합니다 (deletedAt 설정).")
    @DeleteMapping("/me")
    fun deleteAccount(@AuthenticationPrincipal email: String): ApiResponse<Unit> {
        userService.deleteAccount(email)
        return ApiResponse.ok("계정이 삭제되었습니다")
    }
}
