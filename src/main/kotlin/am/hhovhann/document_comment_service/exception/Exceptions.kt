package am.hhovhann.document_comment_service.exception

class DocumentNotFoundException(message: String) : RuntimeException(message)

class InvalidCommentLocationException(message: String) : RuntimeException(message)

class OptimisticLockingException(message: String) : RuntimeException(message)