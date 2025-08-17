package am.hhovhann.document_comment_service.dto.location

import am.hhovhann.document_comment_service.entity.Document
import am.hhovhann.document_comment_service.exception.InvalidCommentLocationException
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory

data class CharRangeLocation(@field:Min(0) val startChar: Int, @field:Min(0) val endChar: Int) :
    CommentLocationStrategy {
    private val logger = LoggerFactory.getLogger(CharRangeLocation::class.java)

    init {
        require(startChar <= endChar) { "Start character must be ≤ end character" }

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
    }
}
