package am.hhovhann.document_comment_service.controller

import am.hhovhann.document_comment_service.dto.DocumentCreateDto
import am.hhovhann.document_comment_service.dto.DocumentResponseDto
import am.hhovhann.document_comment_service.dto.DocumentUpdateDto
import am.hhovhann.document_comment_service.exception.DocumentNotFoundException
import am.hhovhann.document_comment_service.service.DocumentService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(DocumentController::class)
class DocumentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var documentService: DocumentService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun documentService(): DocumentService = mockk()
    }

    @Test
    fun `GET all documents should return 200 with documents list`() {
        // Given
        val documents = listOf(
            createMockDocumentResponse("Doc 1", "Content 1"),
            createMockDocumentResponse("Doc 2", "Content 2")
        )
        every { documentService.getAllDocuments() } returns documents

        // When & Then
        mockMvc.perform(get("/api/documents"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("Doc 1"))
            .andExpect(jsonPath("$[1].title").value("Doc 2"))

        verify { documentService.getAllDocuments() }
    }

    @Test
    fun `GET document by ID should return 200 when document exists`() {
        // Given
        val documentId = UUID.randomUUID()
        val document = createMockDocumentResponse("Test Doc", "Test Content")
        every { documentService.getDocumentById(documentId) } returns document

        // When & Then
        mockMvc.perform(get("/api/documents/$documentId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Test Doc"))
            .andExpect(jsonPath("$.content").value("Test Content"))

        verify { documentService.getDocumentById(documentId) }
    }

    @Test
    fun `GET document by ID should return 404 when document not found`() {
        // Given
        val documentId = UUID.randomUUID()
        every { documentService.getDocumentById(documentId) } throws DocumentNotFoundException("Document not found")

        // When & Then
        mockMvc.perform(get("/api/documents/$documentId"))
            .andExpect(status().isNotFound)

        verify { documentService.getDocumentById(documentId) }
    }

    @Test
    fun `POST create document should return 201 with created document`() {
        // Given
        val createDto = DocumentCreateDto(title = "New Doc", content = "New Content")
        val createdDocument = createMockDocumentResponse("New Doc", "New Content")
        every { documentService.createDocument(createDto) } returns createdDocument

        // When & Then
        mockMvc.perform(
            post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("New Doc"))
            .andExpect(jsonPath("$.content").value("New Content"))

        verify { documentService.createDocument(createDto) }
    }

    @Test
    fun `PUT update document should return 200 with updated document`() {
        // Given
        val documentId = UUID.randomUUID()
        val updateDto = DocumentUpdateDto(title = "Updated Doc", content = "Updated Content", version = 1)
        val updatedDocument = createMockDocumentResponse("Updated Doc", "Content")
        every { documentService.updateDocument(documentId, updateDto) } returns updatedDocument

        // When & Then
        mockMvc.perform(
            put("/api/documents/$documentId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Updated Doc"))

        verify { documentService.updateDocument(documentId, updateDto) }
    }

    @Test
    fun `DELETE document should return 204 when successful`() {
        // Given
        val documentId = UUID.randomUUID()
        every { documentService.deleteDocument(documentId) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/documents/$documentId"))
            .andExpect(status().isNoContent)

        verify { documentService.deleteDocument(documentId) }
    }

    private fun createMockDocumentResponse(title: String, content: String): DocumentResponseDto {
        return DocumentResponseDto(
            id = UUID.randomUUID(),
            title = title,
            content = content,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            commentCount = 0,
            version = 1L
        )
    }
}