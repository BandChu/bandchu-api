package com.bandchu.api.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ProblemDetail> {
        val errorCode = e.errorCode

        log.warn("Business Exception [${errorCode.code}] Occurred: ${e.message}", e)

        val detail = ProblemDetail.forStatusAndDetail(errorCode.httpStatus, errorCode.message).apply {
            type = URI.create("https://api.bandchu.com/errors/${errorCode.docUri}")
            title = errorCode.httpStatus.reasonPhrase
            setProperty("code", errorCode.code)
            setProperty("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString())
        }

        return ResponseEntity.status(errorCode.httpStatus).body(detail)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
        val fieldError = e.bindingResult.fieldErrors.firstOrNull()
        val errorMessage = fieldError?.defaultMessage ?: "요청 데이터가 유효하지 않습니다."

        log.warn("Validation Exception: $errorMessage", e)

        // validation 에러는 필드별로 다른 ErrorCode를 반환하도록 처리
        val errorCode = when (fieldError?.field) {
            "nickname" -> ErrorCode.INVALID_NICKNAME
            "email" -> ErrorCode.INVALID_EMAIL
            "password" -> ErrorCode.INVALID_PASSWORD
            "token" -> ErrorCode.INVALID_TOKEN
            "refreshToken" -> ErrorCode.INVALID_REFRESH_TOKEN
            else -> ErrorCode.INVALID_INPUT
        }

        val detail = ProblemDetail.forStatusAndDetail(errorCode.httpStatus, errorMessage).apply {
            type = URI.create("https://api.bandchu.com/errors/${errorCode.docUri}")
            title = errorCode.httpStatus.reasonPhrase
            setProperty("code", errorCode.code)
            setProperty("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString())
        }

        return ResponseEntity.status(errorCode.httpStatus).body(detail)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllUncaughtExceptions(e: Exception): ResponseEntity<ProblemDetail> {
        log.error("Uncaught Internal Server Error:", e)

        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 에러가 발생하였습니다.").apply {
            type = URI.create("https://api.bandchu.com/errors/internal-server-error")
            title = "Internal Server Error"
            setProperty("code", "INTERNAL_SERVER_ERROR")
            setProperty("timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString())
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(detail)
    }
}