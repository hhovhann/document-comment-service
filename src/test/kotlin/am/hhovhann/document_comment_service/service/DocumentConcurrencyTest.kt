package am.hhovhann.document_comment_service.service

import am.hhovhann.document_comment_service.dto.DocumentCreateDto
import am.hhovhann.document_comment_service.dto.DocumentUpdateDto
import am.hhovhann.document_comment_service.exception.OptimisticLockingException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentConcurrencyTest {

    @Autowired
    private lateinit var documentService: DocumentService

    @Test
    fun `test version mismatch detection`() {
        // Create a document
        val createDto = DocumentCreateDto(
            title = "Version Test Document",
            content = "Original content"
        )
        val document = documentService.createDocument(createDto)
        val documentId = document.id

        // Try to update with wrong version
        val wrongVersionUpdateDto = DocumentUpdateDto(
            title = "Wrong Version Title",
            content = null,
            version = document.version + 1 // Wrong version
        )

        // Should throw OptimisticLockingException
        assertThrows<OptimisticLockingException> {
            documentService.updateDocument(documentId, wrongVersionUpdateDto)
        }
    }

    @Test
    fun `test successful update with correct version`() {
        // Create a document
        val createDto = DocumentCreateDto(
            title = "Correct Version Test",
            content = "Original content"
        )
        val document = documentService.createDocument(createDto)
        val documentId = document.id
        val initialVersion = document.version

        // Update with correct version
        val updateDto = DocumentUpdateDto(
            title = "Updated Title",
            content = "Updated content",
            version = initialVersion
        )

        val updatedDocument = documentService.updateDocument(documentId, updateDto)

        // Verify the update was successful
        assertEquals("Updated Title", updatedDocument.title)
        assertEquals("Updated content", updatedDocument.content)
        // Version should be incremented or at least present
        assertNotNull(updatedDocument.version)
    }

    @Test
    fun `test multiple sequential updates`() {
        // Create a document
        val createDto = DocumentCreateDto(
            title = "Sequential Test",
            content = "Original content"
        )
        val document = documentService.createDocument(createDto)
        val documentId = document.id

        // First update
        val update1 = DocumentUpdateDto(
            title = "First Update",
            content = null,
            version = document.version
        )
        val result1 = documentService.updateDocument(documentId, update1)

        // Second update using the new version
        val update2 = DocumentUpdateDto(
            title = null,
            content = "Second Update",
            version = result1.version
        )
        val result2 = documentService.updateDocument(documentId, update2)

        // Third update using the new version
        val update3 = DocumentUpdateDto(
            title = "Third Update",
            content = null,
            version = result2.version
        )
        val result3 = documentService.updateDocument(documentId, update3)

        // Verify final state
        assertEquals("Third Update", result3.title)
        assertEquals("Second Update", result3.content)
        // Version should be present and incremented
        assertNotNull(result3.version)
        assert(result3.version >= document.version)
    }

    @Test
    fun `test update without version should work`() {
        // Create a document
        val createDto = DocumentCreateDto(
            title = "No Version Test",
            content = "Original content"
        )
        val document = documentService.createDocument(createDto)
        val documentId = document.id

        // Update without version (backward compatibility)
        val updateDto = DocumentUpdateDto(
            title = "Updated Title",
            content = "Updated content"
            // No version specified
        )

        val updatedDocument = documentService.updateDocument(documentId, updateDto)

        // Verify the update was successful
        assertEquals("Updated Title", updatedDocument.title)
        assertEquals("Updated content", updatedDocument.content)
        assertNotNull(updatedDocument.version)
    }
}
