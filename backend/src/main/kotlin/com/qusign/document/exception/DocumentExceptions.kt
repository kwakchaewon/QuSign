package com.qusign.document.exception

class DocumentNotFoundException : RuntimeException("문서를 찾을 수 없습니다")
class StorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
