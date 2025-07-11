package am.hhovhann.document_comment_service.service

import am.hhovhann.document_comment_service.dto.CommentCreateDto
import am.hhovhann.document_comment_service.dto.CommentLocationDto
import am.hhovhann.document_comment_service.dto.CommentResponseDto
import am.hhovhann.document_comment_service.entity.Comment
import am.hhovhann.document_comment_service.entity.CommentLocation
import am.hhovhann.document_comment_service.exception.DocumentNotFoundException
import am.hhovhann.document_comment_service.exception.InvalidCommentLocationException
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

        validateCommentLocation(createDto.location, document.content)

        val comment = Comment(
            content = createDto.content,
            author = createDto.author,
            location = createDto.location.toEntity(),
            document = document
        )

        val savedComment = commentRepository.save(comment)
        logger.info("Saved comment with id={} for documentId={}", savedComment.id, documentId)

        return savedComment.toResponseDto()
    }

    fun getCommentsByAuthor(documentId: UUID, author: String): List<CommentResponseDto> {
        logger.info("Fetching comments by author='{}' for documentId={}", author, documentId)

        val comments = commentRepository.findByDocumentIdAndAuthor(documentId, author)
        logger.debug("Found {} comments by author='{}'", comments.size, author)

        return comments.map { it.toResponseDto() }
    }

    fun getCommentsByParagraph(documentId: UUID, paragraphIndex: Int): List<CommentResponseDto> {
        logger.info("Fetching comments for paragraphIndex={} in documentId={}", paragraphIndex, documentId)

        val comments = commentRepository.findByDocumentIdAndParagraphIndex(documentId, paragraphIndex)
        logger.debug("Found {} comments in paragraph {}", comments.size, paragraphIndex)

        return comments.map { it.toResponseDto() }
    }

    private fun validateCommentLocation(locationDto: CommentLocationDto, documentContent: String) {
        logger.debug("Validating comment location: {}", locationDto)

        locationDto.startChar?.let { startChar ->
            if (startChar >= documentContent.length) {
                logger.warn("Start character {} exceeds document length {}", startChar, documentContent.length)
                throw InvalidCommentLocationException("Start character $startChar exceeds document length")
            }
        }

        locationDto.endChar?.let { endChar ->
            if (endChar >= documentContent.length) {
                logger.warn("End character {} exceeds document length {}", endChar, documentContent.length)
                throw InvalidCommentLocationException("End character $endChar exceeds document length")
            }
        }

        locationDto.paragraphIndex?.let { paragraphIndex ->
            val paragraphs = documentContent.split("\n\n")
            if (paragraphIndex >= paragraphs.size) {
                logger.warn("Paragraph index {} exceeds total paragraphs {}", paragraphIndex, paragraphs.size)
                throw InvalidCommentLocationException("Paragraph index $paragraphIndex exceeds document paragraph count (${paragraphs.size})")
            }
        }

        locationDto.anchorText?.let { anchorText ->
            if (!documentContent.contains(anchorText, ignoreCase = true)) {
                logger.warn("Anchor text '{}' not found in document", anchorText)
                throw InvalidCommentLocationException("Anchor text '$anchorText' not found in document")
            }
        }

        locationDto.lineNumber?.let { lineNumber ->
            val lines = documentContent.split("\n")
            if (lineNumber > lines.size) {
                logger.warn("Line number {} exceeds document line count {}", lineNumber, lines.size)
                throw InvalidCommentLocationException("Line number $lineNumber exceeds document line count (${lines.size})")
            }
        }

        logger.debug("Comment location is valid")
    }

    private fun CommentLocationDto.toEntity(): CommentLocation {
        return CommentLocation(
            startChar = this.startChar,
            endChar = this.endChar,
            paragraphIndex = this.paragraphIndex,
            anchorText = this.anchorText,
            lineNumber = this.lineNumber
        )
    }

    private fun CommentLocation.toDto(): CommentLocationDto {
        return CommentLocationDto(
            startChar = this.startChar,
            endChar = this.endChar,
            paragraphIndex = this.paragraphIndex,
            anchorText = this.anchorText,
            lineNumber = this.lineNumber
        )
    }

    private fun Comment.toResponseDto(): CommentResponseDto {
        return CommentResponseDto(
            id = this.id!!,
            content = this.content,
            author = this.author,
            location = this.location.toDto(),
            createdAt = this.createdAt,
            documentId = this.document.id!!
        )
    }
}
