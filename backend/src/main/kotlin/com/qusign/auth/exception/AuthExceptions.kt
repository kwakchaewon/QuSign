package com.qusign.auth.exception

class EmailAlreadyExistsException(email: String) : RuntimeException("이미 사용 중인 이메일입니다: $email")

class InvalidCredentialsException : RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다")
