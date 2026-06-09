package com.qusign.auth.service

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long,
) {
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8)) }

    fun generateToken(email: String, role: String = "USER"): String = Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + expirationMs))
        .signWith(key)
        .compact()

    fun extractEmail(token: String): String =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject

    fun extractRole(token: String): String =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload["role"] as? String ?: "USER"

    fun isValid(token: String): Boolean = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
        true
    } catch (e: JwtException) {
        false
    }
}
