package com.qusign.signature.exception

class SignatureRequestNotFoundException : RuntimeException("서명 요청을 찾을 수 없습니다")
class DuplicateSignatureRequestException : RuntimeException("이미 대기 중인 서명 요청이 있습니다")
class SignatureRequestExpiredException : RuntimeException("서명 요청이 만료되었습니다")
class SignatureRequestAlreadySignedException : RuntimeException("이미 서명된 요청입니다")
class UnauthorizedSignerException : RuntimeException("서명 권한이 없습니다")
class SignatureVerificationFailedException : RuntimeException("서명 검증에 실패했습니다")
class InvalidSignaturePasswordException : RuntimeException("비밀번호가 올바르지 않습니다")
class NoQuSignMetadataException : RuntimeException("QuSign 서명이 없는 문서입니다")
class SignatureRequestCancelledException : RuntimeException("취소된 서명 요청입니다")
class SignatureRequestNotCancellableException : RuntimeException("PENDING 상태의 서명 요청만 취소할 수 있습니다")
