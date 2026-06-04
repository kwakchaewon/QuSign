package com.qusign.auth.controller

import com.qusign.auth.dto.ChangePasswordRequest
import com.qusign.auth.dto.NotificationSettingsResponse
import com.qusign.auth.dto.UpdateNotificationSettingsRequest
import com.qusign.auth.dto.UserProfileResponse
import com.qusign.auth.service.UserService
import com.qusign.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping("/me")
    fun getProfile(@AuthenticationPrincipal email: String): ApiResponse<UserProfileResponse> =
        ApiResponse.ok(userService.getProfile(email))

    @PutMapping("/password")
    fun changePassword(
        @AuthenticationPrincipal email: String,
        @Valid @RequestBody request: ChangePasswordRequest,
    ): ApiResponse<Unit> {
        userService.changePassword(email, request.currentPassword, request.newPassword)
        return ApiResponse.ok("비밀번호가 변경되었습니다")
    }

    @PutMapping("/notification-settings")
    fun updateNotificationSettings(
        @AuthenticationPrincipal email: String,
        @RequestBody request: UpdateNotificationSettingsRequest,
    ): ApiResponse<NotificationSettingsResponse> =
        ApiResponse.ok(userService.updateNotificationSettings(email, request))

    @GetMapping("/search")
    fun searchUsers(
        @AuthenticationPrincipal email: String,
        @RequestParam q: String,
    ): ApiResponse<List<String>> = ApiResponse.ok(userService.searchUsers(email, q))

    @DeleteMapping("/me")
    fun deleteAccount(@AuthenticationPrincipal email: String): ApiResponse<Unit> {
        userService.deleteAccount(email)
        return ApiResponse.ok("계정이 삭제되었습니다")
    }
}
