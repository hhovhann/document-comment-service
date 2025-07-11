package am.hhovhann.document_comment_service.controller

import am.hhovhann.document_comment_service.dto.CommentCreateDto
import am.hhovhann.document_comment_service.dto.CommentResponseDto
import am.hhovhann.document_comment_service.service.CommentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/api/documents/{documentId}/comments")
@Tag(name = "Comments", description = "Comment management operations")
class CommentController(
    private val commentService: CommentService
) {
    private val logger = LoggerFactory.getLogger(CommentController::class.java)

    @GetMapping
    @Operation(summary = "Get comments for document", description = "Retrieve all comments for a specific document")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully retrieved comments"),
        ApiResponse(responseCode = "404", description = "Document not found")
    )
    fun getCommentsByDocumentId(
        @Parameter(description = "Document ID")
        @PathVariable documentId: UUID,
        @Parameter(description = "Optional filter by author")
        @RequestParam(required = false) author: String?,
        @Parameter(description = "Optional filter by paragraph index")
        @RequestParam(required = false) paragraphIndex: Int?
    ): ResponseEntity<List<CommentResponseDto>> {
        logger.info(
            "GET /api/documents/{}/comments called with author='{}', paragraphIndex={}",
            documentId,
            author,
            paragraphIndex
        )

        val comments = when {
            author != null -> {
                logger.debug("Filtering by author: {}", author)
                commentService.getCommentsByAuthor(documentId, author)
            }

            paragraphIndex != null -> {
                logger.debug("Filtering by paragraphIndex: {}", paragraphIndex)
                commentService.getCommentsByParagraph(documentId, paragraphIndex)
            }

            else -> commentService.getCommentsByDocumentId(documentId)
        }

        logger.info("Returning {} comments for documentId={}", comments.size, documentId)
        return ResponseEntity.ok(comments)
    }

    @PostMapping
    @Operation(summary = "Create comment", description = "Add a new comment to a document")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Comment created successfully"),
        ApiResponse(responseCode = "404", description = "Document not found"),
        ApiResponse(responseCode = "400", description = "Invalid input data or comment location")
    )
    fun createComment(
        @Parameter(description = "Document ID")
        @PathVariable documentId: UUID,
        @Valid @RequestBody createDto: CommentCreateDto
    ): ResponseEntity<CommentResponseDto> {
        logger.info("POST /api/documents/{}/comments called by author='{}'", documentId, createDto.author)

        val comment = commentService.createComment(documentId, createDto)

        logger.info("Created comment with id={} for documentId={}", comment.id, documentId)
        return ResponseEntity.status(HttpStatus.CREATED).body(comment)
    }
}
