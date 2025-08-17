package am.hhovhann.document_comment_service.service.integration

import am.hhovhann.document_comment_service.dto.DocumentCreateDto
import am.hhovhann.document_comment_service.dto.DocumentUpdateDto
import am.hhovhann.document_comment_service.entity.DocumentBlock
import am.hhovhann.document_comment_service.exception.OptimisticLockingException
import am.hhovhann.document_comment_service.repository.DocumentRepository
import am.hhovhann.document_comment_service.service.DocumentService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class DocumentServiceIntegrationTest @Autowired constructor(
    private val documentService: DocumentService,
    private val documentRepository: DocumentRepository
) {

    @Test
    fun `create, fetch and delete document`() {
        val created = documentService.createDocument(DocumentCreateDto("Title", "Content"))
        assertNotNull(created.id)

        val fetched = documentService.getDocumentById(created.id)
        assertEquals("Title", fetched.title)

        documentService.deleteDocument(created.id)
        assertFalse(documentRepository.findById(created.id).isPresent)
    }

    @Test
    fun `update document successfully`() {
        val created = documentService.createDocument(DocumentCreateDto("Old", "Content"))
        val dto = DocumentUpdateDto("New", "Updated", version = created.version)

        val updated = documentService.updateDocument(created.id, dto)

        assertEquals("New", updated.title)
        assertEquals("Updated", updated.content)
    }

    @Test
    fun `version mismatch detection`() {
        val doc = documentService.createDocument(DocumentCreateDto("Version Test", "Original"))
        val wrongVersionUpdate = DocumentUpdateDto("Wrong", null, version = doc.version + 1)

        assertThrows<OptimisticLockingException> {
            documentService.updateDocument(doc.id, wrongVersionUpdate)
        }
    }

    @Test
    fun `successful update with correct version`() {
        val doc = documentService.createDocument(DocumentCreateDto("Correct Version Test", "Original"))
        val dto = DocumentUpdateDto("Updated Title", "Updated content", version = doc.version)

        val updated = documentService.updateDocument(doc.id, dto)

        assertEquals("Updated Title", updated.title)
        assertEquals("Updated content", updated.content)
        assertNotNull(updated.version)
    }

    @Test
    fun `multiple sequential updates`() {
        val doc = documentService.createDocument(DocumentCreateDto("Seq Test", "Original"))

        val u1 = documentService.updateDocument(doc.id, DocumentUpdateDto("First", null, version = doc.version))
        val fresh1 = documentService.getDocumentById(doc.id)   // re-fetch to get latest version

        val u2 = documentService.updateDocument(doc.id, DocumentUpdateDto(null, "Second", version = fresh1.version))
        val fresh2 = documentService.getDocumentById(doc.id)

        val u3 = documentService.updateDocument(doc.id, DocumentUpdateDto("Third", null, version = fresh2.version))
        val fresh3 = documentService.getDocumentById(doc.id)

        assertEquals("Third", fresh3.title)
        assertEquals("Second", fresh3.content)
        assertTrue(fresh3.version > doc.version)
    }

    /**
     * Test case to verify the full lifecycle of a document that includes content blocks.
     * This test covers:
     * 1. Creating a document with blocks.
     * 2. Fetching the document and verifying the blocks are present.
     * 3. Updating the document with a new set of blocks.
     * 4. Fetching the updated document and verifying the new blocks are saved.
     * 5. Deleting the document.
     */
    @Test
    fun `create, fetch, delete and update document with blocks`() {
        // --- 1. Create a document with blocks ---
        val createDto = DocumentCreateDto(
            title = "My Sample Document with blocks",
            content = "This is the first paragraph...",
            blocks = listOf(
                DocumentBlock("para-1", "paragraph", "Paragraph 1."),
                DocumentBlock("para-2", "paragraph", "Paragraph 2 with some data."),
                DocumentBlock("fig-3a", "figure", "Diagram showing architecture.")
            )
        )
        val created = documentService.createDocument(createDto)
        assertNotNull(created.id)
        assertNotNull(created.blocks)
        assertEquals(3, created.blocks.size)
        assertEquals("para-1", created.blocks.get(0).id)
        assertEquals("Paragraph 1.", created.blocks.get(0)?.content)

        // --- 2. Fetch the document and verify blocks ---
        val fetchedAfterCreate = documentService.getDocumentById(created.id)
        assertEquals("My Sample Document with blocks", fetchedAfterCreate.title)
        assertEquals("This is the first paragraph...", fetchedAfterCreate.content)
        assertNotNull(fetchedAfterCreate.blocks)
        assertEquals(3, fetchedAfterCreate.blocks?.size)
        assertEquals("fig-3a", fetchedAfterCreate.blocks?.get(2)?.id)

        // --- 3. Update the document with new blocks ---
        val updateDto = DocumentUpdateDto(
            title = "Updated Document Title",
            content = "This is the updated content...",
            blocks = listOf(
                DocumentBlock("fig-3a", "figure", "Diagram showing architecture.")
            ),
            version = fetchedAfterCreate.version
        )
        val updated = documentService.updateDocument(created.id, updateDto)

        // --- 4. Assert the update was successful and blocks were replaced ---
        assertEquals("Updated Document Title", updated.title)
        assertEquals("This is the updated content...", updated.content)
        assertNotNull(updated.blocks)
        assertEquals(1, updated.blocks.size)
        assertEquals("fig-3a", updated.blocks.get(0).id)
        assertEquals("Diagram showing architecture.", updated.blocks.get(0).content)
        assertTrue(updated.version == fetchedAfterCreate.version)

        // --- 5. Delete the document and verify it's gone ---
        documentService.deleteDocument(created.id)
        assertFalse(documentRepository.findById(created.id).isPresent)
    }
}
