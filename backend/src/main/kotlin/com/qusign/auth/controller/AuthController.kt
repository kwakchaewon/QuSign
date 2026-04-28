package com.qusign.auth.controller

import com.qusign.auth.dto.LoginRequest
import com.qusign.auth.dto.RegisterRequest
import com.qusign.auth.dto.TokenResponse
import com.qusign.auth.service.AuthService
import com.qusign.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): ApiResponse<Unit> {
        authService.register(request.email, request.password)
        return ApiResponse.ok("회원가입이 완료되었습니다")
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<TokenResponse> {
        val token = authService.login(request.email, request.password)
        return ApiResponse.ok(TokenResponse(accessToken = token))
    }
}
