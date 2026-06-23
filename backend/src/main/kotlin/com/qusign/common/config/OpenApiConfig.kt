package com.qusign.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("QuSign API")
                .version("1.0.0")
                .description(
                    """
                    PQC(후양자 암호) 기반 전자서명 서비스 API

                    **인증 방식**: 로그인 후 발급된 JWT 액세스 토큰을 `Authorization: Bearer <token>` 헤더에 포함하여 요청합니다.

                    **서명 알고리즘**: CRYSTALS-Dilithium3 (ML-DSA) + RFC 3161 타임스탬프
                    """.trimIndent()
                )
        )
        .components(
            Components().addSecuritySchemes(
                "bearerAuth",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("로그인 후 발급된 JWT 토큰을 입력하세요")
            )
        )
        .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
}
