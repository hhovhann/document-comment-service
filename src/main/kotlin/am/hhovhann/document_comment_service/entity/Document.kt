package am.hhovhann.document_comment_service.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "documents")
data class Document(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0L,

    @field:NotBlank(message = "Title cannot be blank")
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    @Column(nullable = false)
    var title: String,

    @field:NotBlank(message = "Content cannot be blank")
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(
        mappedBy = "document",
        cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val comments: MutableList<Comment> = mutableListOf()
)
