package com.qusign.auth.repository

import com.qusign.auth.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun findTop8ByEmailContainingIgnoreCaseAndDeletedAtIsNullOrderByEmail(email: String): List<User>

    // Admin: 전체 사용자 목록 (탈퇴자 제외, 페이징)
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<User>

    // Admin: 이메일 검색 + 페이징
    fun findByEmailContainingIgnoreCaseAndDeletedAtIsNull(email: String, pageable: Pageable): Page<User>

    // Admin: 총 활성 사용자 수
    fun countByDeletedAtIsNull(): Long
}
