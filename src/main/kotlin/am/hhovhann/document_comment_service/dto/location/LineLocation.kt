package am.hhovhann.document_comment_service.dto.location

import am.hhovhann.document_comment_service.entity.Document
import am.hhovhann.document_comment_service.exception.InvalidCommentLocationException
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory

data class LineLocation(@field:Min(1) val lineNumber: Int) : CommentLocationStrategy {

    private val logger = LoggerFactory.getLogger(LineLocation::class.java)

    init {
        require(lineNumber > 0) { "Line number must be positive" }
    }

    override fun validate(document: Document?) {
        val lines = document?.content?.split("\n")
        lines?.size?.let {
            if (lineNumber > it) {
                logger.warn("Line number {} exceeds document line count {}", lineNumber, lines.size)
                throw InvalidCommentLocationException("Line number $lineNumber exceeds document line count (${lines.size})")
            }
        }
    }
}
