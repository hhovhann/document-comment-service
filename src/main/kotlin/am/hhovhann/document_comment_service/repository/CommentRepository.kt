package am.hhovhann.document_comment_service.repository

import am.hhovhann.document_comment_service.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommentRepository : JpaRepository<Comment, UUID> {
    fun findByDocumentIdOrderByCreatedAtAsc(documentId: UUID): List<Comment>
}
