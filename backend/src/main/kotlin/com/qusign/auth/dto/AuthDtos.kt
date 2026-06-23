package com.qusign.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "회원가입 요청")
data class RegisterRequest(
    @Schema(description = "이메일 주소", example = "user@example.com")
    @field:Email(message = "유효한 이메일 형식이어야 합니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    @Schema(description = "비밀번호 (8자 이상, 영문+숫자 포함)", example = "password1")
    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
        message = "비밀번호는 영문자와 숫자를 포함해야 합니다"
    )
    val password: String,
)

@Schema(description = "로그인 요청")
data class LoginRequest(
    @Schema(description = "이메일 주소", example = "user@example.com")
    @field:NotBlank val email: String,
    @Schema(description = "비밀번호", example = "password1")
    @field:NotBlank val password: String,
)

@Schema(description = "JWT 토큰 응답")
data class TokenResponse(
    @Schema(description = "JWT 액세스 토큰")
    val accessToken: String,
    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String = "Bearer",
)
