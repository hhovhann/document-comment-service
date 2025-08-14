-- Add indexes for optimistic locking and performance optimization
-- This migration adds additional indexes to improve query performance

-- Add index on version for optimistic locking queries
CREATE INDEX idx_documents_version ON documents(version);

-- Add composite index for document search with title and updated_at
CREATE INDEX idx_documents_title_updated_at ON documents(title, updated_at DESC);

-- Add index for comment location queries
CREATE INDEX idx_comments_location ON comments(start_char, end_char, paragraph_index);

-- Add index for comment creation time
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);
