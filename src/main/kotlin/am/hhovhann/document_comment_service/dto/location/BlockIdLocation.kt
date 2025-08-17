package am.hhovhann.document_comment_service.dto.location

import am.hhovhann.document_comment_service.entity.Document
import org.slf4j.LoggerFactory

data class BlockIdLocation(val blockId: String) : CommentLocationStrategy {

    private val logger = LoggerFactory.getLogger(BlockIdLocation::class.java)

    init {
        require(blockId.isNotBlank()) { "Block ID cannot be blank" }
    }

    override fun validate(document: Document?) {
        document?.blocks?.any { it.id == blockId }?.let {
            require(it) {
                "Block ID $blockId not found in document"
            }
        }
    }
}
