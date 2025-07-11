package am.hhovhann.document_comment_service.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.Min

@Embeddable
data class CommentLocation(
    // Character-based positioning
    @field:Min(0, message = "Start character must be non-negative")
    @Column(name = "start_char")
    val startChar: Int? = null,

    @field:Min(0, message = "End character must be non-negative")
    @Column(name = "end_char")
    val endChar: Int? = null,

    // Paragraph-based positioning
    @field:Min(0, message = "Paragraph index must be non-negative")
    @Column(name = "paragraph_index")
    val paragraphIndex: Int? = null,

    // Anchor text positioning
    @Column(name = "anchor_text", length = 500)
    val anchorText: String? = null,

    // Line number positioning
    @field:Min(1, message = "Line number must be positive")
    @Column(name = "line_number")
    val lineNumber: Int? = null
)
