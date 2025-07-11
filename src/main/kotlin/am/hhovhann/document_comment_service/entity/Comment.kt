package am.hhovhann.document_comment_service.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "comments")
data class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @field:NotBlank(message = "Content cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @field:NotBlank(message = "Author cannot be blank")
    @field:Size(max = 100, message = "Author name must be less than 100 characters")
    @Column(nullable = false)
    var author: String,

    @Embedded
    var location: CommentLocation,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    var document: Document
)
