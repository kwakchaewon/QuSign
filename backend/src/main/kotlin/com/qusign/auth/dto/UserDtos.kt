package com.qusign.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다")
    val currentPassword: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
        message = "비밀번호는 영문자와 숫자를 포함해야 합니다"
    )
    val newPassword: String,
)

data class UpdateNotificationSettingsRequest(
    val notifySignRequest: Boolean,
    val notifySignDone: Boolean,
    val notifyWeekly: Boolean,
    val notifyMarketing: Boolean,
)

data class NotificationSettingsResponse(
    val notifySignRequest: Boolean,
    val notifySignDone: Boolean,
    val notifyWeekly: Boolean,
    val notifyMarketing: Boolean,
)

data class UserProfileResponse(
    val email: String,
    val notifySignRequest: Boolean,
    val notifySignDone: Boolean,
    val notifyWeekly: Boolean,
    val notifyMarketing: Boolean,
)
