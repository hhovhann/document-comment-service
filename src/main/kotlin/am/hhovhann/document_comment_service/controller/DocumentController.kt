package am.hhovhann.document_comment_service.controller

import am.hhovhann.document_comment_service.dto.DocumentCreateDto
import am.hhovhann.document_comment_service.dto.DocumentResponseDto
import am.hhovhann.document_comment_service.dto.DocumentUpdateDto
import am.hhovhann.document_comment_service.service.DocumentService
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
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document management operations")
class DocumentController(private val documentService: DocumentService) {
    private val logger = LoggerFactory.getLogger(DocumentController::class.java)

    @GetMapping
    @Operation(summary = "Get all documents", description = "Retrieve all documents ordered by last updated")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Successfully retrieved documents")])
    fun getAllDocuments(): ResponseEntity<List<DocumentResponseDto>> {
        logger.info("GET /api/documents")
        val documents = documentService.getAllDocuments()
        logger.info("Returning {} documents", documents.size)
        return ResponseEntity.status(HttpStatus.OK).body(documents)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieve a specific document by its ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved document"),
            ApiResponse(responseCode = "404", description = "Document not found")
        ]
    )
    fun getDocumentById(@Parameter(description = "Document ID") @PathVariable id: UUID): ResponseEntity<DocumentResponseDto> {
        logger.info("GET /api/documents/{} called", id)
        val document = documentService.getDocumentById(id)
        logger.info("Retrieved document with id={}", id)
        return ResponseEntity.status(HttpStatus.OK).body(document)
    }

    @PostMapping
    @Operation(summary = "Create new document", description = "Create a new document")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Document created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input data")
        ]
    )
    fun createDocument(@Valid @RequestBody createDto: DocumentCreateDto): ResponseEntity<DocumentResponseDto> {
        logger.info("POST /api/documents called to create new document with title='{}'", createDto.title)
        val document = documentService.createDocument(createDto)
        logger.info("Created new document with id={}", document.id)
        return ResponseEntity.status(HttpStatus.CREATED).body(document)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update document", description = "Update an existing document")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Document updated successfully"),
        ApiResponse(responseCode = "404", description = "Document not found"),
        ApiResponse(responseCode = "400", description = "Invalid input data")
    )
    fun updateDocument(
        @Parameter(description = "Document ID") @PathVariable id: UUID,
        @Valid @RequestBody updateDto: DocumentUpdateDto
    ): ResponseEntity<DocumentResponseDto> {
        logger.info("PUT /api/documents/{} called to update document", id)
        val document = documentService.updateDocument(id, updateDto)
        logger.info("Updated document with id={}", document.id)
        return ResponseEntity.status(HttpStatus.OK).body(document)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document", description = "Delete a document and all its comments")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Document deleted successfully"),
            ApiResponse(responseCode = "404", description = "Document not found")
        ]
    )
    fun deleteDocument(@Parameter(description = "Document ID") @PathVariable id: UUID): ResponseEntity<Unit> {
        logger.info("DELETE /api/documents/{} called", id)
        documentService.deleteDocument(id)
        logger.info("Deleted document with id={}", id)
        return ResponseEntity.noContent().build()
    }
}
