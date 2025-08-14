-- Initial schema creation for document commenting service
-- This migration creates the base tables for documents and comments

-- Create documents table
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create comments table
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id UUID NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    start_char INTEGER,
    end_char INTEGER,
    paragraph_index INTEGER,
    line_number INTEGER,
    anchor_text VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_documents_updated_at ON documents(updated_at DESC);
CREATE INDEX idx_documents_title ON documents(title);
CREATE INDEX idx_comments_document_id ON comments(document_id);
CREATE INDEX idx_comments_author ON comments(author);
CREATE INDEX idx_comments_paragraph_index ON comments(paragraph_index);

-- Create trigger to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_documents_updated_at 
    BEFORE UPDATE ON documents 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
