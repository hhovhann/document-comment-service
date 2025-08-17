package am.hhovhann.document_comment_service.service

import am.hhovhann.document_comment_service.dto.CommentCreateDto
import am.hhovhann.document_comment_service.dto.CommentResponseDto
import am.hhovhann.document_comment_service.entity.Comment
import am.hhovhann.document_comment_service.exception.DocumentNotFoundException
import am.hhovhann.document_comment_service.repository.CommentRepository
import am.hhovhann.document_comment_service.repository.DocumentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.slf4j.LoggerFactory

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val documentRepository: DocumentRepository
) {
    private val logger = LoggerFactory.getLogger(CommentService::class.java)

    fun getCommentsByDocumentId(documentId: UUID): List<CommentResponseDto> {
        logger.info("Fetching comments for documentId={}", documentId)

        if (!documentRepository.existsById(documentId)) {
            logger.warn("Document not found with id={}", documentId)
            throw DocumentNotFoundException("Document not found with id: $documentId")
        }
        val comments = commentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
        logger.debug("Found {} comments for documentId={}", comments.size, documentId)

        return comments.map { it.toResponseDto() }
    }

    fun createComment(documentId: UUID, createDto: CommentCreateDto): CommentResponseDto {
        logger.info("Creating comment on documentId={} by author='{}'", documentId, createDto.author)

        val document = documentRepository.findById(documentId)
            .orElseThrow {
                logger.error("Failed to find document with id={}", documentId)
                DocumentNotFoundException("Document not found with id: $documentId")
            }

        createDto.location.validate(document)

        val comment = Comment(
            comment = createDto.content,
            author = createDto.author,
            location = createDto.location,
            document = document
        )

        val savedComment = commentRepository.save(comment)
        logger.info("Saved comment with id={} for documentId={}", savedComment.id, documentId)

        return savedComment.toResponseDto()
    }

    private fun Comment.toResponseDto(): CommentResponseDto {
        return CommentResponseDto(
            id = this.id!!,
            content = this.comment,
            author = this.author,
            location = this.location,
            createdAt = this.createdAt,
            documentId = this.document.id!!
        )
    }
}
