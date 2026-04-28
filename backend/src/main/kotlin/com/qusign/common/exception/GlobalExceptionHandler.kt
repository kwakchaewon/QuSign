package com.qusign.common.exception

import com.qusign.auth.exception.EmailAlreadyExistsException
import com.qusign.auth.exception.InvalidCredentialsException
import com.qusign.common.response.ApiResponse
import com.qusign.document.exception.DocumentNotFoundException
import com.qusign.document.exception.StorageException
import com.qusign.signature.exception.*
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleEmailAlreadyExists(e: EmailAlreadyExistsException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentials(e: InvalidCredentialsException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(DocumentNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleDocumentNotFound(e: DocumentNotFoundException) = ApiResponse.error(e.message!!)

    @ExceptionHandler(StorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleStorage(e: StorageException) = ApiResponse.error(e.message!!)

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

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpected(e: Exception) = ApiResponse.error("서버 오류가 발생했습니다")
}
