package am.hhovhann.document_comment_service.repository

import am.hhovhann.document_comment_service.entity.Document
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DocumentRepository : JpaRepository<Document, UUID> {

    @Query("SELECT d FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    fun findByTitleContainingIgnoreCase(title: String): List<Document>

    @Query("SELECT d FROM Document d ORDER BY d.updatedAt DESC")
    fun findAllOrderByUpdatedAtDesc(): List<Document>
}
