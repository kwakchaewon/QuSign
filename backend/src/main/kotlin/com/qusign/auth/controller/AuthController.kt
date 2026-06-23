package com.qusign.auth.controller

import com.qusign.auth.dto.LoginRequest
import com.qusign.auth.dto.RegisterRequest
import com.qusign.auth.dto.TokenResponse
import com.qusign.auth.service.AuthService
import com.qusign.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "회원가입 · 로그인")
@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @Operation(summary = "회원가입", description = "이메일·비밀번호로 계정을 생성합니다. 가입 시 PQC 키페어가 자동 생성됩니다.")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "회원가입 성공"),
        SwaggerApiResponse(responseCode = "400", description = "입력값 오류 (이메일 형식 불일치, 비밀번호 조건 미충족 등)"),
        SwaggerApiResponse(responseCode = "409", description = "이미 사용 중인 이메일"),
    )
    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): ApiResponse<Unit> {
        authService.register(request.email, request.password)
        return ApiResponse.ok("회원가입이 완료되었습니다")
    }

    @Operation(summary = "로그인", description = "이메일·비밀번호로 인증하여 JWT 액세스 토큰을 발급받습니다.")
    @ApiResponses(
        SwaggerApiResponse(
            responseCode = "200", description = "로그인 성공",
            content = [Content(schema = Schema(implementation = TokenResponse::class))]
        ),
        SwaggerApiResponse(responseCode = "401", description = "인증 실패 (이메일 또는 비밀번호 오류)"),
    )
    @SecurityRequirements
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse> {
        val token = authService.login(request.email, request.password)
        return ApiResponse.ok(TokenResponse(accessToken = token))
    }
}
