package am.hhovhann.document_comment_service.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.*

data class CommentResponseDto(
    val id: UUID,
    val content: String,
    val author: String,
    val location: CommentLocationDto,
    val createdAt: LocalDateTime,
    val documentId: UUID
)

data class CommentCreateDto(
    @field:NotBlank(message = "Content cannot be blank")
    val content: String,

    @field:NotBlank(message = "Author cannot be blank")
    @field:Size(max = 100, message = "Author name must be less than 100 characters")
    val author: String,

    @field:Valid
    val location: CommentLocationDto
)

data class CommentLocationDto(
    @field:Min(0, message = "Start character must be non-negative")
    val startChar: Int? = null,

    @field:Min(0, message = "End character must be non-negative")
    val endChar: Int? = null,

    @field:Min(0, message = "Paragraph index must be non-negative")
    val paragraphIndex: Int? = null,

    val anchorText: String? = null,

    @field:Min(1, message = "Line number must be positive")
    val lineNumber: Int? = null
) {
    init {
        // Validation stays in DTO where it belongs
        require(startChar != null || paragraphIndex != null || anchorText != null || lineNumber != null) {
            "At least one location reference must be provided"
        }

        // Validate character range
        if (startChar != null && endChar != null) {
            require(startChar <= endChar) {
                "Start character must be less than or equal to end character"
            }
        }
    }
}