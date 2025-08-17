package am.hhovhann.document_comment_service.dto.location

import am.hhovhann.document_comment_service.entity.Document
import am.hhovhann.document_comment_service.exception.InvalidCommentLocationException
import org.slf4j.LoggerFactory

data class CompositeLocation(
    val startChar: Int,
    val endChar: Int,
    val paragraphIndex: Int,
    val anchorText: String
) : CommentLocationStrategy {
    private val logger = LoggerFactory.getLogger(CompositeLocation::class.java)

    init {
        require(startChar <= endChar) { "Start character must be ≤ end character" }
        require(paragraphIndex >= 0) { "Paragraph index cannot be negative" }
        require(anchorText.isNotBlank()) { "Anchor must not be blank" }
    }

    override fun validate(document: Document?) {

        document?.content?.let { documentContent ->
            if (startChar >= documentContent.length) {
                logger.warn("Start character {} exceeds document length {}", startChar, documentContent.length)
                throw InvalidCommentLocationException("Start character $startChar exceeds document length")
            }
        }

        document?.content?.let { documentContent ->
            if (endChar >= documentContent.length) {
                logger.warn("End character {} exceeds document length {}", endChar, documentContent.length)
                throw InvalidCommentLocationException("End character $endChar exceeds document length")
            }
        }

        val paragraphs = document?.content?.split("\n\n")
        paragraphs?.let {
            if (paragraphIndex >= it.size) {
                logger.warn("Paragraph index {} exceeds total paragraphs {}", paragraphIndex, paragraphs.size)
                throw InvalidCommentLocationException("Paragraph index $paragraphIndex exceeds document paragraph count (${paragraphs.size})")
            }
        }

        document?.content?.contains(anchorText, ignoreCase = true)?.let { documentContent ->
            if (!documentContent) {
                logger.warn("Anchor text '{}' not found in document", anchorText)
                throw InvalidCommentLocationException("Anchor text '$anchorText' not found in document")
            }
        }
    }
}
