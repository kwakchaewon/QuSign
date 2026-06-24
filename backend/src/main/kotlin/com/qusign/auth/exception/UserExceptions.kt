package com.qusign.auth.exception

class InvalidCurrentPasswordException : RuntimeException("현재 비밀번호가 올바르지 않습니다")

class AccountDeletedException : RuntimeException("탈퇴한 계정입니다")

class PasswordChangeNotAllowedException : RuntimeException("이 계정은 비밀번호를 변경할 수 없습니다")
