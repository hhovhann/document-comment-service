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
class DocumentService(private val documentRepository: DocumentRepository) {
    private val logger = LoggerFactory.getLogger(DocumentService::class.java)

    fun getAllDocuments(): List<DocumentResponseDto> {
        logger.info("Fetching all documents ordered by updatedAt DESC")
        val documents = documentRepository.findAllOrderByUpdatedAtDesc()
        logger.debug("Retrieved {} documents", documents.size)

        return documents.map { it.toResponseDto() }
    }

    fun getDocumentById(id: UUID): DocumentResponseDto {
        logger.info("Fetching document with id={}", id)
        val document = documentRepository
            .findById(id)
            .orElseThrow {
                logger.warn("Document not found with id={}", id)
                DocumentNotFoundException("Document not found with id: $id")
            }

        return document.toResponseDto()
    }

    fun createDocument(createDto: DocumentCreateDto): DocumentResponseDto {
        logger.info("Creating new document with title='{}'", createDto.title)
        val documentToSave = Document(title = createDto.title, content = createDto.content, blocks = createDto.blocks)
        val savedDocument = documentRepository.save(documentToSave)
        logger.info("Document created with id={}", savedDocument.id)

        return savedDocument.toResponseDto()
    }

    fun updateDocument(id: UUID, updateDto: DocumentUpdateDto): DocumentResponseDto {
        logger.info("Updating document with id={}, version={}", id, updateDto.version)
        val existingDocument = documentRepository.findById(id)
            .orElseThrow {
                logger.warn("Document not found with id={}", id)
                DocumentNotFoundException("Document not found with id: $id")
            }

        // Check version value for optimistic locking
        updateDto.version.let { expectedVersion ->
            if (existingDocument.version != expectedVersion) {
                logger.warn(
                    "Version conflict for document id={}. Expected: {}, Actual: {}",
                    id,
                    expectedVersion,
                    existingDocument.version
                )
                throw OptimisticLockingException(
                    "Document has been modified by another user. " +
                            "Expected version: $expectedVersion, Current version: ${existingDocument.version}. " +
                            "Please refresh and try again."
                )
            }
        }

        // Apply Updates
        updateDto.title?.let {
            logger.debug("Updating title to '{}'", it)
            existingDocument.title = it
        }
        updateDto.content?.let {
            logger.debug("Updating content length to {}", it.length)
            existingDocument.content = it
        }
        updateDto.blocks.let {
            logger.debug("Updating blocks size to {}", it.size)
            existingDocument.blocks = it
        }

        try {
            val updatedDocument = documentRepository.save(existingDocument)
            logger.info(
                "Document with id={} updated successfully to version {}",
                updatedDocument.id,
                updatedDocument.version
            )
            return updatedDocument.toResponseDto()
        } catch (exception: OptimisticLockingFailureException) {
            logger.warn("Optimistic locking failure for document id={}, exception={}", id, exception.message)
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

    private fun Document.toResponseDto(): DocumentResponseDto {
        return DocumentResponseDto(
            id = this.id!!,
            title = this.title,
            content = this.content,
            blocks = this.blocks,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            commentCount = this.comments.size,
            version = this.version
        )
    }
}
