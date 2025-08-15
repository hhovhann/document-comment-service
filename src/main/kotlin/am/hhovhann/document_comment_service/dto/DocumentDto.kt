package am.hhovhann.document_comment_service.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.*

data class DocumentResponseDto(
    val id: UUID,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val commentCount: Int = 0,
    val version: Long
)

data class DocumentCreateDto(
    @field:NotBlank(message = "Title cannot be blank")
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    val title: String,

    @field:NotBlank(message = "Content cannot be blank")
    val content: String
)

data class DocumentUpdateDto(
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    val title: String?,

    val content: String?,

    @field:PositiveOrZero(message = "Content must be positive")
    val version: Long
)
