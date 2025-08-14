package am.hhovhann.document_comment_service.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException::class)
    fun handleDocumentNotFoundException(ex: DocumentNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Document Not Found",
            message = ex.message ?: "Document not found",
            path = "/api/documents"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(InvalidCommentLocationException::class)
    fun handleInvalidCommentLocationException(ex: InvalidCommentLocationException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Invalid Comment Location",
            message = ex.message ?: "Invalid comment location",
            path = "/api/comments"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(OptimisticLockingException::class)
    fun handleOptimisticLockingException(ex: OptimisticLockingException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Optimistic Locking Conflict",
            message = ex.message ?: "Document has been modified by another user",
            path = "/api/documents"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "An unexpected error occurred",
            path = "/api"
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
