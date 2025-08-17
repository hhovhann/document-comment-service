package am.hhovhann.document_comment_service.dto.location

import am.hhovhann.document_comment_service.entity.Document
import am.hhovhann.document_comment_service.exception.InvalidCommentLocationException
import org.slf4j.LoggerFactory

data class AnchorLocation(val anchorText: String) : CommentLocationStrategy {
    private val logger = LoggerFactory.getLogger(AnchorLocation::class.java)

    init {
        require(anchorText.isNotBlank()) { "Anchor text cannot be blank" }
    }

    override fun validate(document: Document?) {
        document?.content?.contains(anchorText, ignoreCase = true)?.let { documentContent ->
            if (!documentContent) {
                logger.warn("Anchor text '{}' not found in document", anchorText)
                throw InvalidCommentLocationException("Anchor text '$anchorText' not found in document")
            }
        }
    }
}
