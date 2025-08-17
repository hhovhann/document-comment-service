package am.hhovhann.document_comment_service.dto.location

import am.hhovhann.document_comment_service.entity.Document
import am.hhovhann.document_comment_service.exception.InvalidCommentLocationException
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory

data class ParagraphLocation(@field:Min(0) val paragraphIndex: Int) : CommentLocationStrategy {

    private val logger = LoggerFactory.getLogger(ParagraphLocation::class.java)

    init {
        require(paragraphIndex >= 0) { "Paragraph index cannot be negative" }
    }

    override fun validate(document: Document?) {
        val paragraphs = document?.content?.split("\n\n")
        paragraphs?.let {
            if (paragraphIndex >= it.size) {
                logger.warn("Paragraph index {} exceeds total paragraphs {}", paragraphIndex, paragraphs.size)
                throw InvalidCommentLocationException("Paragraph index $paragraphIndex exceeds document paragraph count (${paragraphs.size})")
            }
        }
    }
}
