package am.hhovhann.document_comment_service.service

import am.hhovhann.document_comment_service.dto.DocumentCreateDto
import am.hhovhann.document_comment_service.dto.DocumentResponseDto
import am.hhovhann.document_comment_service.dto.DocumentUpdateDto
import am.hhovhann.document_comment_service.entity.Document
import am.hhovhann.document_comment_service.exception.DocumentNotFoundException
import am.hhovhann.document_comment_service.exception.OptimisticLockingException
import am.hhovhann.document_comment_service.repository.DocumentRepository
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import org.slf4j.LoggerFactory

@Service
@Transactional
class DocumentService(
    private val documentRepository: DocumentRepository
) {
    private val logger = LoggerFactory.getLogger(DocumentService::class.java)

    fun getAllDocuments(): List<DocumentResponseDto> {
        logger.info("Fetching all documents ordered by updatedAt DESC")
        val documents = documentRepository.findAllOrderByUpdatedAtDesc()
        logger.debug("Retrieved {} documents", documents.size)
        return documents.map { it.toResponseDto() }
    }

    fun getDocumentById(id: UUID): DocumentResponseDto {
        logger.info("Fetching document with id={}", id)
        val document = documentRepository.findById(id)
            .orElseThrow {
                logger.warn("Document not found with id={}", id)
                DocumentNotFoundException("Document not found with id: $id")
            }
        return document.toResponseDto()
    }

    fun createDocument(createDto: DocumentCreateDto): DocumentResponseDto {
        logger.info("Creating new document with title='{}'", createDto.title)

        val document = Document(
            title = createDto.title,
            content = createDto.content
        )
        val savedDocument = documentRepository.save(document)
        logger.info("Document created with id={}", savedDocument.id)
        return savedDocument.toResponseDto()
    }

    fun updateDocument(id: UUID, updateDto: DocumentUpdateDto): DocumentResponseDto {
        logger.info("Updating document with id={}, version={}", id, updateDto.version)
        val document = documentRepository.findById(id)
            .orElseThrow {
                logger.warn("Document not found with id={}", id)
                DocumentNotFoundException("Document not found with id: $id")
            }

        // Check version for optimistic locking
        updateDto.version?.let { expectedVersion ->
            if (document.version != expectedVersion) {
                logger.warn("Version conflict for document id={}. Expected: {}, Actual: {}",
                    id, expectedVersion, document.version)
                throw OptimisticLockingException(
                    "Document has been modified by another user. " +
                    "Expected version: $expectedVersion, Current version: ${document.version}. " +
                    "Please refresh and try again."
                )
            }
        }

        // Apply updates
        updateDto.title?.let {
            logger.debug("Updating title to '{}'", it)
            document.title = it
        }
        updateDto.content?.let {
            logger.debug("Updating content length to {}", it.length)
            document.content = it
        }

        try {
            val updatedDocument = documentRepository.save(document)
            logger.info("Document with id={} updated successfully to version {}", updatedDocument.id, updatedDocument.version)
            return updatedDocument.toResponseDto()
        } catch (e: OptimisticLockingFailureException) {
            logger.warn("Optimistic locking failure for document id={}", id)
            throw OptimisticLockingException("Document has been modified by another user. Please refresh and try again.")
        }
    }

    fun deleteDocument(id: UUID) {
        logger.info("Deleting document with id={}", id)
        if (!documentRepository.existsById(id)) {
            logger.warn("Document with id={} does not exist", id)
            throw DocumentNotFoundException("Document not found with id: $id")
        }
        documentRepository.deleteById(id)
        logger.info("Document with id={} deleted", id)
    }

    fun searchDocuments(title: String): List<DocumentResponseDto> {
        logger.info("Searching documents with title containing '{}'", title)
        val results = documentRepository.findByTitleContainingIgnoreCase(title)
        logger.debug("Found {} documents matching title='{}'", results.size, title)
        return results.map { it.toResponseDto() }
    }

    private fun Document.toResponseDto(): DocumentResponseDto {
        return DocumentResponseDto(
            id = this.id!!,
            title = this.title,
            content = this.content,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            commentCount = this.comments.size,
            version = this.version
        )
    }
}
