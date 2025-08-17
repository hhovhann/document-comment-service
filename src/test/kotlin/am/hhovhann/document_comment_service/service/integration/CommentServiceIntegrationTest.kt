package am.hhovhann.document_comment_service.service.integration

import am.hhovhann.document_comment_service.dto.CommentCreateDto
import am.hhovhann.document_comment_service.dto.DocumentCreateDto
import am.hhovhann.document_comment_service.dto.location.AnchorLocation
import am.hhovhann.document_comment_service.dto.location.CharRangeLocation
import am.hhovhann.document_comment_service.dto.location.CompositeLocation
import am.hhovhann.document_comment_service.dto.location.ParagraphLocation
import am.hhovhann.document_comment_service.exception.DocumentNotFoundException
import am.hhovhann.document_comment_service.exception.InvalidCommentLocationException
import am.hhovhann.document_comment_service.service.CommentService
import am.hhovhann.document_comment_service.service.DocumentService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("test")
class CommentServiceIntegrationTest @Autowired constructor(private val documentService: DocumentService, private val commentService: CommentService) {

    @Test
    fun `getCommentsByDocumentId should return comments`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "Some text here"))
        commentService.createComment(doc.id, CommentCreateDto("Comment 1", "Author 1", CharRangeLocation(0, 4)))
        commentService.createComment(doc.id, CommentCreateDto("Comment 2", "Author 2", CharRangeLocation(5, 9)))

        val comments = commentService.getCommentsByDocumentId(doc.id)

        val charRangeLocation = comments[0].location as? CharRangeLocation

        assertEquals(2, comments.size)
        assertEquals("Comment 1", comments[0].content)
        assertNotNull(charRangeLocation)
        assertEquals(0, charRangeLocation.startChar)
        assertEquals(4, charRangeLocation.endChar)
    }

    @Test
    fun `getCommentsByDocumentId should throw when document not exists`() {
        val fakeId = UUID.randomUUID()
        assertThrows<DocumentNotFoundException> {
            commentService.getCommentsByDocumentId(fakeId)
        }
    }

    @Test
    fun `createComment should persist with valid location`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "This is a test document content"))
        val dto = CommentCreateDto(
            "Test comment",
            "Tester",
            CompositeLocation(startChar = 0, endChar = 4, paragraphIndex = 0,"content")
        )

        val saved = commentService.createComment(doc.id, dto)
        val compositeLocation = saved.location as? CompositeLocation

        assertEquals("Test comment", saved.content)
        assertEquals("Tester", saved.author)
        assertNotNull(saved.id)
        assertNotNull(compositeLocation)
        assertNotNull(compositeLocation.startChar)
        assertNotNull(compositeLocation.endChar)
        assertNotNull(compositeLocation.paragraphIndex)
        assertNotNull(compositeLocation.anchorText)
    }

    @Test
    fun `create comment with char range`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "This is sample text"))
        val dto = CommentCreateDto("Char range comment", "Tester", CharRangeLocation(startChar = 0, endChar = 4))

        val saved = commentService.createComment(doc.id, dto)

        val charRangeLocation = saved.location as? CharRangeLocation

        assertEquals("Char range comment", saved.content)
        assertNotNull(charRangeLocation)
        assertNotNull(charRangeLocation.startChar)
        assertNotNull(charRangeLocation.endChar)
    }

    @Test
    fun `create comment with paragraph index`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "Paragraph one.\n\nParagraph two."))
        val dto = CommentCreateDto("Paragraph comment", "Tester", ParagraphLocation(paragraphIndex = 1))

        val saved = commentService.createComment(doc.id, dto)

        val paragraphLocation = saved.location as? ParagraphLocation

        assertEquals("Paragraph comment", saved.content)
        assertNotNull(paragraphLocation)
        assertEquals(1, paragraphLocation.paragraphIndex)
    }

    @Test
    fun `create comment with anchor text`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "This is a test document"))
        val dto = CommentCreateDto("Anchor text comment", "Tester", AnchorLocation(anchorText = "test"))

        val saved = commentService.createComment(doc.id, dto)

        val anchorLocation = saved.location as? AnchorLocation

        assertEquals("Anchor text comment", saved.content)
        assertNotNull(anchorLocation)
        assertEquals("test", anchorLocation.anchorText)
    }

    @Test
    fun `create comment with combined location`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "This is a test document"))
        val dto = CommentCreateDto(
            "Combined location comment",
            "Tester",
            CompositeLocation(startChar = 5, endChar = 7, paragraphIndex = 0, anchorText = "is")
        )

        val saved = commentService.createComment(doc.id, dto)
        val compositeLocation = saved.location as? CompositeLocation


        assertEquals("Combined location comment", saved.content)

        assertNotNull(compositeLocation)
         assertEquals(0, compositeLocation.paragraphIndex)
         assertEquals("is", compositeLocation.anchorText)
    }

    @Test
    fun `get comments by document id`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "Some text here"))
        commentService.createComment(doc.id, CommentCreateDto("Comment 1", "Author 1", CharRangeLocation(0, 4)))
        commentService.createComment(doc.id, CommentCreateDto("Comment 2", "Author 2", AnchorLocation(anchorText = "here")))

        val comments = commentService.getCommentsByDocumentId(doc.id)
        assertEquals(2, comments.size)
    }

    @Test
    fun `createComment should throw DocumentNotFoundException when document not exists`() {
        val dto = CommentCreateDto("Comment", "Author", CharRangeLocation(0, 4))
        assertThrows<DocumentNotFoundException> {
            commentService.createComment(UUID.randomUUID(), dto)
        }
    }

    @Test
    fun `createComment should throw InvalidCommentLocationException when startChar exceeds content length`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "Short"))
        val dto = CommentCreateDto("Comment", "Author", CharRangeLocation(100, 105))

        assertThrows<InvalidCommentLocationException> {
            commentService.createComment(doc.id, dto)
        }
    }

    @Test
    fun `createComment should throw InvalidCommentLocationException when anchor text not found`() {
        val doc = documentService.createDocument(DocumentCreateDto("Doc", "This is a test"))
        val dto = CommentCreateDto("Comment", "Author", AnchorLocation(anchorText = "nonexistent text"))

        assertThrows<InvalidCommentLocationException> {
            commentService.createComment(doc.id, dto)
        }
    }
}
