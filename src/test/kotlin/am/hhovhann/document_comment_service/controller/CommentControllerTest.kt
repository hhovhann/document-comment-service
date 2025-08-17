package am.hhovhann.document_comment_service.controller

import am.hhovhann.document_comment_service.dto.CommentCreateDto
import am.hhovhann.document_comment_service.dto.CommentResponseDto
import am.hhovhann.document_comment_service.dto.location.CharRangeLocation
import am.hhovhann.document_comment_service.exception.DocumentNotFoundException
import am.hhovhann.document_comment_service.service.CommentService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(CommentController::class)
class CommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var commentService: CommentService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun commentService(): CommentService = mockk()
    }

    @Test
    fun `GET comments should return 200 with comments list`() {
        // Given
        val documentId = UUID.randomUUID()
        val comments = listOf(
            createMockCommentResponse("Comment 1", "Author 1"),
            createMockCommentResponse("Comment 2", "Author 2")
        )
        every { commentService.getCommentsByDocumentId(documentId) } returns comments

        // When & Then
        mockMvc.perform(get("/api/documents/$documentId/comments"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].content").value("Comment 1"))
            .andExpect(jsonPath("$[1].content").value("Comment 2"))

        verify { commentService.getCommentsByDocumentId(documentId) }
    }

    @Test
    fun `GET comments should return 404 when document not found`() {
        // Given
        val documentId = UUID.randomUUID()
        every { commentService.getCommentsByDocumentId(documentId) } throws DocumentNotFoundException("Document not found")

        // When & Then
        mockMvc.perform(get("/api/documents/$documentId/comments"))
            .andExpect(status().isNotFound)

        verify { commentService.getCommentsByDocumentId(documentId) }
    }

    @Test
    fun `POST create comment should return 201 with created comment`() {
        // Given
        val documentId = UUID.randomUUID()
        val locationDto = CharRangeLocation(startChar = 0, endChar = 4)
        val createDto = CommentCreateDto(
            content = "Test comment",
            author = "Test Author",
            location = locationDto
        )
        val createdComment = createMockCommentResponse("Test comment", "Test Author")
        every { commentService.createComment(documentId, createDto) } returns createdComment

        // When & Then
        mockMvc.perform(
            post("/api/documents/$documentId/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.content").value("Test comment"))
            .andExpect(jsonPath("$.author").value("Test Author"))

        verify { commentService.createComment(documentId, createDto) }
    }

    private fun createMockCommentResponse(content: String, author: String): CommentResponseDto {
        return CommentResponseDto(
            id = UUID.randomUUID(),
            content = content,
            author = author,
            location = CharRangeLocation(startChar = 0, endChar = 4),
            createdAt = LocalDateTime.now(),
            documentId = UUID.randomUUID()
        )
    }
}