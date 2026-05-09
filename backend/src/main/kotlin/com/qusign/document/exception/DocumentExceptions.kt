package com.qusign.document.exception

class DocumentNotFoundException : RuntimeException("문서를 찾을 수 없습니다")
class StorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
class BatchTooManyFilesException(max: Int = 5) : RuntimeException("최대 ${max}개 파일까지 업로드할 수 있습니다")
