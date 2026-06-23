package com.qusign.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "비밀번호 변경 요청")
data class ChangePasswordRequest(
    @Schema(description = "현재 비밀번호", example = "oldPassword1")
    @field:NotBlank(message = "현재 비밀번호는 필수입니다")
    val currentPassword: String,

    @Schema(description = "새 비밀번호 (8자 이상, 영문+숫자 포함)", example = "newPassword1")
    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
        message = "비밀번호는 영문자와 숫자를 포함해야 합니다"
    )
    val newPassword: String,
)

@Schema(description = "알림 설정 변경 요청")
data class UpdateNotificationSettingsRequest(
    @Schema(description = "서명 요청 알림 수신 여부", example = "true")
    val notifySignRequest: Boolean,
    @Schema(description = "서명 완료 알림 수신 여부", example = "true")
    val notifySignDone: Boolean,
    @Schema(description = "주간 리포트 알림 수신 여부", example = "false")
    val notifyWeekly: Boolean,
    @Schema(description = "마케팅 알림 수신 여부", example = "false")
    val notifyMarketing: Boolean,
)

@Schema(description = "알림 설정 응답")
data class NotificationSettingsResponse(
    @Schema(description = "서명 요청 알림 수신 여부")
    val notifySignRequest: Boolean,
    @Schema(description = "서명 완료 알림 수신 여부")
    val notifySignDone: Boolean,
    @Schema(description = "주간 리포트 알림 수신 여부")
    val notifyWeekly: Boolean,
    @Schema(description = "마케팅 알림 수신 여부")
    val notifyMarketing: Boolean,
)

@Schema(description = "사용자 프로필 응답")
data class UserProfileResponse(
    @Schema(description = "이메일 주소", example = "user@example.com")
    val email: String,
    @Schema(description = "서명 요청 알림 수신 여부")
    val notifySignRequest: Boolean,
    @Schema(description = "서명 완료 알림 수신 여부")
    val notifySignDone: Boolean,
    @Schema(description = "주간 리포트 알림 수신 여부")
    val notifyWeekly: Boolean,
    @Schema(description = "마케팅 알림 수신 여부")
    val notifyMarketing: Boolean,
)
