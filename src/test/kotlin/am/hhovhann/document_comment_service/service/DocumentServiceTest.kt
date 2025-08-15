package am.hhovhann.document_comment_service.service

import am.hhovhann.document_comment_service.dto.DocumentCreateDto
import am.hhovhann.document_comment_service.dto.DocumentUpdateDto
import am.hhovhann.document_comment_service.entity.Document
import am.hhovhann.document_comment_service.exception.DocumentNotFoundException
import am.hhovhann.document_comment_service.repository.DocumentRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest
class DocumentServiceTest {

    private val documentRepository = mockk<DocumentRepository>()
    private lateinit var documentService: DocumentService

    @BeforeEach
    fun setUp() {
        documentService = DocumentService(documentRepository)
    }

    @Test
    fun `getAllDocuments should return all documents ordered by updatedAt desc`() {
        // Given
        val documents = listOf(
            createMockDocument("Doc 1", "Content 1"),
            createMockDocument("Doc 2", "Content 2")
        )
        every { documentRepository.findAllOrderByUpdatedAtDesc() } returns documents

        // When
        val result = documentService.getAllDocuments()

        // Then
        assertEquals(2, result.size)
        assertEquals("Doc 1", result[0].title)
        assertEquals("Doc 2", result[1].title)
        verify { documentRepository.findAllOrderByUpdatedAtDesc() }
    }

    @Test
    fun `getDocumentById should return document when exists`() {
        // Given
        val documentId = UUID.randomUUID()
        val document = createMockDocument("Test Doc", "Test Content")
        every { documentRepository.findById(documentId) } returns Optional.of(document)

        // When
        val result = documentService.getDocumentById(documentId)

        // Then
        assertEquals("Test Doc", result.title)
        assertEquals("Test Content", result.content)
        verify { documentRepository.findById(documentId) }
    }

    @Test
    fun `getDocumentById should throw DocumentNotFoundException when not exists`() {
        // Given
        val documentId = UUID.randomUUID()
        every { documentRepository.findById(documentId) } returns Optional.empty()

        // When & Then
        assertThrows<DocumentNotFoundException> {
            documentService.getDocumentById(documentId)
        }
        verify { documentRepository.findById(documentId) }
    }

    @Test
    fun `createDocument should save and return new document`() {
        // Given
        val createDto = DocumentCreateDto(title = "New Doc", content = "New Content")
        val savedDocument = createMockDocument("New Doc", "New Content")
        every { documentRepository.save(any<Document>()) } returns savedDocument

        // When
        val result = documentService.createDocument(createDto)

        // Then
        assertEquals("New Doc", result.title)
        assertEquals("New Content", result.content)
        verify { documentRepository.save(any<Document>()) }
    }

    @Test
    fun `updateDocument should update existing document`() {
        // Given
        val documentId = UUID.randomUUID()
        val existingDocument = createMockDocument("Old Title", "Old Content")
        val updateDto = DocumentUpdateDto(title = "Updated Title", content = "Updated Content", version = 0)
        val updatedDocument = createMockDocument("Updated Title", "Updated Content")

        every { documentRepository.findById(documentId) } returns Optional.of(existingDocument)
        every { documentRepository.save(any<Document>()) } returns updatedDocument

        // When
        val result = documentService.updateDocument(documentId, updateDto)

        // Then
        assertEquals("Updated Title", result.title)
        assertEquals("Updated Content", result.content)
        verify { documentRepository.findById(documentId) }
        verify { documentRepository.save(any<Document>()) }
    }

    @Test
    fun `updateDocument should throw DocumentNotFoundException when not exists`() {
        // Given
        val documentId = UUID.randomUUID()
        val updateDto = DocumentUpdateDto(title = "Updated Title", content = "Updated Content", version = 1)
        every { documentRepository.findById(documentId) } returns Optional.empty()

        // When & Then
        assertThrows<DocumentNotFoundException> {
            documentService.updateDocument(documentId, updateDto)
        }
        verify { documentRepository.findById(documentId) }
    }

    @Test
    fun `deleteDocument should delete existing document`() {
        // Given
        val documentId = UUID.randomUUID()
        every { documentRepository.existsById(documentId) } returns true
        every { documentRepository.deleteById(documentId) } just runs

        // When
        documentService.deleteDocument(documentId)

        // Then
        verify { documentRepository.existsById(documentId) }
        verify { documentRepository.deleteById(documentId) }
    }

    @Test
    fun `deleteDocument should throw DocumentNotFoundException when not exists`() {
        // Given
        val documentId = UUID.randomUUID()
        every { documentRepository.existsById(documentId) } returns false

        // When & Then
        assertThrows<DocumentNotFoundException> {
            documentService.deleteDocument(documentId)
        }
        verify { documentRepository.existsById(documentId) }
    }

    private fun createMockDocument(title: String,
                                   content: String): Document {
        return Document(
            id = UUID.randomUUID(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            comments = mutableListOf(),
            title = title,
            content = content
        )
    }
}