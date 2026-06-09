package com.qusign.common.exception

import com.qusign.auth.exception.AccountDeletedException
import com.qusign.auth.exception.AccountDisabledException
import com.qusign.auth.exception.EmailAlreadyExistsException
import com.qusign.auth.exception.InvalidCredentialsException
import com.qusign.auth.exception.InvalidCurrentPasswordException
import com.qusign.common.response.ApiResponse
import com.qusign.document.exception.AlreadySignedDocumentException
import com.qusign.document.exception.BatchTooManyFilesException
import com.qusign.document.exception.DocumentNotFoundException
import com.qusign.document.exception.InvalidFileTypeException
import com.qusign.document.exception.StorageException
import com.qusign.signature.exception.DuplicateSignatureRequestException
import com.qusign.signature.exception.*
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyExists(e: EmailAlreadyExistsException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(InvalidCurrentPasswordException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidCurrentPassword(e: InvalidCurrentPasswordException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(AccountDeletedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccountDeleted(e: AccountDeletedException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(AccountDisabledException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccountDisabled(e: AccountDisabledException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(DocumentNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDocumentNotFound(e: DocumentNotFoundException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(InvalidFileTypeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidFileType(e: InvalidFileTypeException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(AlreadySignedDocumentException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleAlreadySignedDocument(e: AlreadySignedDocumentException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(BatchTooManyFilesException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBatchTooMany(e: BatchTooManyFilesException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(StorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleStorage(e: StorageException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    fun handleMaxUploadSize(e: MaxUploadSizeExceededException) =
        ApiResponse.error("파일 크기가 50MB를 초과합니다")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ApiResponse<Unit> {
        val message = e.bindingResult.allErrors
            .filterIsInstance<FieldError>()
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ApiResponse.error(message)
    }

    @ExceptionHandler(SignatureRequestNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleSignatureRequestNotFound(e: SignatureRequestNotFoundException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(DuplicateSignatureRequestException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateSignatureRequest(e: DuplicateSignatureRequestException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(SignatureRequestExpiredException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleSignatureExpired(e: SignatureRequestExpiredException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(SignatureRequestAlreadySignedException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleAlreadySigned(e: SignatureRequestAlreadySignedException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(UnauthorizedSignerException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleUnauthorizedSigner(e: UnauthorizedSignerException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(SignatureVerificationFailedException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleVerificationFailed(e: SignatureVerificationFailedException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(InvalidSignaturePasswordException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidSignaturePassword(e: InvalidSignaturePasswordException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(NoQuSignMetadataException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNoQuSignMetadata(e: NoQuSignMetadataException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(SignatureRequestCancelledException::class)
    @ResponseStatus(HttpStatus.GONE)
    fun handleCancelled(e: SignatureRequestCancelledException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(SignatureRequestNotCancellableException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleNotCancellable(e: SignatureRequestNotCancellableException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpected(e: Exception) = ApiResponse.error("서버 오류가 발생했습니다")
}
