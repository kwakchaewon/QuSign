package com.qusign.common.exception

import com.qusign.auth.exception.EmailAlreadyExistsException
import com.qusign.auth.exception.InvalidCredentialsException
import com.qusign.common.response.ApiResponse
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

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ApiResponse<Unit> {
        val message = e.bindingResult.allErrors
            .filterIsInstance<FieldError>()
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ApiResponse.error(message)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUnexpected(e: Exception) = ApiResponse.error("서버 오류가 발생했습니다")
}
