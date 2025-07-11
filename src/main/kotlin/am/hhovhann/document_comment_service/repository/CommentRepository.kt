package am.hhovhann.document_comment_service.repository

import am.hhovhann.document_comment_service.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommentRepository : JpaRepository<Comment, UUID> {

    fun findByDocumentIdOrderByCreatedAtAsc(documentId: UUID): List<Comment>

    @Query("SELECT c FROM Comment c WHERE c.document.id = :documentId AND c.author = :author ORDER BY c.createdAt ASC")
    fun findByDocumentIdAndAuthor(documentId: UUID, author: String): List<Comment>

    @Query("SELECT c FROM Comment c WHERE c.document.id = :documentId AND c.location.paragraphIndex = :paragraphIndex ORDER BY c.createdAt ASC")
    fun findByDocumentIdAndParagraphIndex(documentId: UUID, paragraphIndex: Int): List<Comment>
}
