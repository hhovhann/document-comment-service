package am.hhovhann.document_comment_service.service.unit

import am.hhovhann.document_comment_service.dto.CommentCreateDto
import am.hhovhann.document_comment_service.dto.location.AnchorLocation
import am.hhovhann.document_comment_service.dto.location.BlockIdLocation
import am.hhovhann.document_comment_service.dto.location.CharRangeLocation
import am.hhovhann.document_comment_service.dto.location.CompositeLocation
import am.hhovhann.document_comment_service.entity.Comment
import am.hhovhann.document_comment_service.entity.Document
import am.hhovhann.document_comment_service.entity.DocumentBlock
import am.hhovhann.document_comment_service.exception.DocumentNotFoundException
import am.hhovhann.document_comment_service.exception.InvalidCommentLocationException
import am.hhovhann.document_comment_service.repository.CommentRepository
import am.hhovhann.document_comment_service.repository.DocumentRepository
import am.hhovhann.document_comment_service.service.CommentService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest
class CommentServiceTest {

    private val commentRepository = mockk<CommentRepository>()
    private val documentRepository = mockk<DocumentRepository>()
    private lateinit var commentService: CommentService

    @BeforeEach
    fun setUp() {
        commentService = CommentService(commentRepository, documentRepository)
    }

    @Test
    fun `getCommentsByDocumentId should return comments when document exists`() {
        // Given
        val documentId = UUID.randomUUID()
        val comments = listOf(
            createMockComment("Comment 1", "Author 1"),
            createMockComment("Comment 2", "Author 2")
        )
        every { documentRepository.existsById(documentId) } returns true
        every { commentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId) } returns comments

        // When
        val result = commentService.getCommentsByDocumentId(documentId)

        // Then
        assertEquals(2, result.size)
        assertEquals("Comment 1", result[0].content)
        assertEquals("Author 1", result[0].author)
        verify { documentRepository.existsById(documentId) }
        verify { commentRepository.findByDocumentIdOrderByCreatedAtAsc(documentId) }
    }

    @Test
    fun `getCommentsByDocumentId should throw DocumentNotFoundException when document not exists`() {
        // Given
        val documentId = UUID.randomUUID()
        every { documentRepository.existsById(documentId) } returns false

        // When & Then
        assertThrows<DocumentNotFoundException> {
            commentService.getCommentsByDocumentId(documentId)
        }
        verify { documentRepository.existsById(documentId) }
    }

    @Test
    fun `createComment should create comment with valid location`() {
        // Given
        val documentId = UUID.randomUUID()
        val document = createMockDocument("Test Doc", "This is a test document content", listOf())
        val locationDto = CompositeLocation(startChar = 0, endChar = 4, paragraphIndex = 0, "content")
        val createDto = CommentCreateDto(
            content = "Test comment",
            author = "Test Author",
            location = locationDto
        )
        val savedComment = createMockComment("Test comment", "Test Author")

        every { documentRepository.findById(documentId) } returns Optional.of(document)
        every { commentRepository.save(any<Comment>()) } returns savedComment

        // When
        val result = commentService.createComment(documentId, createDto)

        // Then
        assertEquals("Test comment", result.content)
        assertEquals("Test Author", result.author)
        verify { documentRepository.findById(documentId) }
        verify { commentRepository.save(any<Comment>()) }
    }

    @Test
    fun `createComment should create comment with block-based location`() {
        // Given
        val documentId = UUID.randomUUID()
        val document = createMockDocument(
            "Test Doc",
            "Content",
            listOf(DocumentBlock("fig-3a", "figure", "Diagram showing architecture."))
        )
        val locationDto = BlockIdLocation(blockId = "fig-3a")
        val createDto = CommentCreateDto(
            content = "Comment on block",
            author = "Block Author",
            location = locationDto
        )
        val savedComment = createMockCommentWithBlockLocation(
            "Comment on block",
            "Block Author",
            "fig-3a"
        )
        every { documentRepository.findById(documentId) } returns Optional.of(document)
        every { commentRepository.save(any<Comment>()) } returns savedComment

        // When
        val result = commentService.createComment(documentId, createDto)

        // Then
        assertEquals("Comment on block", result.content)
        val location = result.location as? BlockIdLocation
        assertNotNull(location)
        assertEquals("fig-3a", location.blockId)
        verify { documentRepository.findById(documentId) }
        verify { commentRepository.save(any<Comment>()) }
    }

    @Test
    fun `createComment should throw DocumentNotFoundException when document not exists`() {
        // Given
        val documentId = UUID.randomUUID()
        val locationDto = CharRangeLocation(startChar = 0, endChar = 4)
        val createDto = CommentCreateDto(
            content = "Test comment",
            author = "Test Author",
            location = locationDto
        )
        every { documentRepository.findById(documentId) } returns Optional.empty()

        // When & Then
        assertThrows<DocumentNotFoundException> {
            commentService.createComment(documentId, createDto)
        }
        verify { documentRepository.findById(documentId) }
    }

    @Test
    fun `createComment should throw InvalidCommentLocationException when startChar exceeds content length`() {
        // Given
        val documentId = UUID.randomUUID()
        val document = createMockDocument("Test Doc", "Short", listOf())
        val locationDto = CharRangeLocation(startChar = 100, endChar = 105)
        val createDto = CommentCreateDto(
            content = "Test comment",
            author = "Test Author",
            location = locationDto
        )
        every { documentRepository.findById(documentId) } returns Optional.of(document)

        // When & Then
        assertThrows<InvalidCommentLocationException> {
            commentService.createComment(documentId, createDto)
        }
        verify { documentRepository.findById(documentId) }
    }

    @Test
    fun `createComment should throw InvalidCommentLocationException when anchor text not found`() {
        // Given
        val documentId = UUID.randomUUID()
        val document = createMockDocument("Test Doc", "This is a test document", listOf())
        val locationDto = AnchorLocation(anchorText = "nonexistent text")
        val createDto = CommentCreateDto(
            content = "Test comment",
            author = "Test Author",
            location = locationDto
        )
        every { documentRepository.findById(documentId) } returns Optional.of(document)

        // When & Then
        assertThrows<InvalidCommentLocationException> {
            commentService.createComment(documentId, createDto)
        }
        verify { documentRepository.findById(documentId) }
    }

    private fun createMockComment(content: String, author: String): Comment {
        return Comment(
            id = UUID.randomUUID(),
            createdAt = LocalDateTime.now(),
            comment = content,
            author = author,
            location = AnchorLocation(anchorText = "nonexistent text"),
            document = createMockDocument("Test", "Test content", listOf())
        )
    }

    private fun createMockCommentWithBlockLocation(content: String, author: String, blockId: String): Comment {
        return Comment(
            id = UUID.randomUUID(),
            createdAt = LocalDateTime.now(),
            comment = content,
            author = author,
            location = BlockIdLocation(blockId = blockId),
            document = createMockDocument("Test", "Test content", listOf(DocumentBlock(blockId, "figure", "Content")))
        )
    }

    private fun createMockDocument(title: String, content: String, blocks: List<DocumentBlock>): Document {
        return Document(
            id = UUID.randomUUID(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            comments = mutableListOf(),
            title = title,
            content = content,
            blocks = blocks
        )
    }
}