package it.polito.students.crm.exception_handlers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidException(e: MethodArgumentNotValidException): ResponseEntity<HashMap<String, String>> {
        val errors = HashMap<String, String>()
        val bindingResult: BindingResult = e.bindingResult
        for (error: FieldError in bindingResult.fieldErrors) {
            errors[error.field] = error.defaultMessage ?: "Unknown error"
        }

        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }

}