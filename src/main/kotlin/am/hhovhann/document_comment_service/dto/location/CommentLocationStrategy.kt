package am.hhovhann.document_comment_service.dto.location

import am.hhovhann.document_comment_service.entity.Document
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type" // must be present in JSON
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AnchorLocation::class, name = "anchor"),
    JsonSubTypes.Type(value = ParagraphLocation::class, name = "paragraph"),
    JsonSubTypes.Type(value = LineLocation::class, name = "line"),
    JsonSubTypes.Type(value = CharRangeLocation::class, name = "charRange"),
    JsonSubTypes.Type(value = BlockIdLocation::class, name = "block"),
    JsonSubTypes.Type(value = CompositeLocation::class, name = "composite"),
)
sealed interface CommentLocationStrategy {
    fun validate(content: Document?)
}
