package com.qusign.signature.exception

class SignatureRequestNotFoundException : RuntimeException("서명 요청을 찾을 수 없습니다")
class SignatureRequestExpiredException : RuntimeException("서명 요청이 만료되었습니다")
class SignatureRequestAlreadySignedException : RuntimeException("이미 서명된 요청입니다")
class UnauthorizedSignerException : RuntimeException("서명 권한이 없습니다")
class SignatureVerificationFailedException : RuntimeException("서명 검증에 실패했습니다")
