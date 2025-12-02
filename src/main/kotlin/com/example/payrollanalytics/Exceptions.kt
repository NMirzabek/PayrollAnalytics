package com.example.payrollanalytics

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

data class ApiError(
    val message: String,
    val status: Int,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

class NotFoundException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)

@RestControllerAdvice
class GlobalExceptionHandler {

    //4004
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiError> {
        val body = ApiError(
            message = ex.message ?: "Resource not found",
            status = HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }

    //400
    @ExceptionHandler(BadRequestException::class, IllegalArgumentException::class)
    fun handleBadRequest(ex: RuntimeException): ResponseEntity<ApiError> {
        val body = ApiError(
            message = ex.message ?: "Bad request",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    // 500
    @ExceptionHandler(Exception::class)
    fun handleOther(ex: Exception): ResponseEntity<ApiError> {
        val body = ApiError(
            message = ex.message ?: "Internal server error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        return ResponseEntity(body, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}