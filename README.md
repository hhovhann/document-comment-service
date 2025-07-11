# Collaborative Document Commenting Backend

A complete Spring Boot backend system that allows users to create documents and add location-specific comments.

## 🛠 Tech Stack
- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.3
- **ORM**: Hibernate / JPA
- **Database**: PostgreSQL
- **API Docs**: Swagger
- **Build Tool**: Gradle
- **Java Version**: 21

## 📁 Project Structure

```
src/main/kotlin/com/example/documentcomments/
├── DocumentCommentsApplication.kt
├── controller/
│   ├── DocumentController.kt
│   └── CommentController.kt
├── service/
│   ├── DocumentService.kt
│   └── CommentService.kt
├── entity/
│   ├── Document.kt
│   ├── Comment.kt
│   └── CommentLocation.kt
├── repository/
│   ├── DocumentRepository.kt
│   └── CommentRepository.kt
├── dto/
│   ├── DocumentDto.kt
│   └── CommentDto.kt
└── exception/
    ├── Exceptions.kt
    └── GlobalExceptionHandler.kt
```

## 🚀 Setup Instructions

### Prerequisites
- Java 21 or higher
- PostgreSQL 12 or higher
- Gradle 8.0 or higher


### Database Setup
1. Create a PostgreSQL database:
```sql
CREATE DATABASE document_comments;
```
2. Run Postgres docker container before running the application to test it locally
   ```docker-compose up --build```

### Running the Application

```bash
# Clone the repository
git clone git@github.com:hhovhann/document-comment-service.git
cd document-commenting-service

# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```
The application will start on `http://localhost:8080`

### WIP: Install and Run the Application as a Docker Container

# Install Docker Desktop for your system
(Download Docker Desktop)[https://docs.docker.com/desktop/]

# Run docker compose locally
```docker-compose up --build```

# Run kubernates (locally need to install minicube)
```kubectl apply -f kubernetes/```

### API Documentation
Once running, access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## 📍 Comment-to-Document Location Mapping

### Design Decision
I chose a **flexible multi-strategy approach** using an embedded `CommentLocation` entity that supports multiple location reference types. This allows comments to be anchored to specific places in documents using different strategies based on use case.

### Location Strategies

#### 1. **Character-Based Positioning** (Most Precise)
```json
{
  "startChar": 150,
  "endChar": 200
}
```
- **Use Case**: Precise selection of text ranges
- **How it works**: References exact character positions in the document content
- **Validation**: Ensures positions don't exceed document length
- **Best for**: Text selections, inline comments

#### 2. **Paragraph-Based Positioning** (Structural)
```json
{
  "paragraphIndex": 2
}
```
- **Use Case**: Comments on entire paragraphs or sections
- **How it works**: References paragraph number (0-indexed, split by `\n\n`)
- **Validation**: Ensures paragraph index exists in document
- **Best for**: Section-level feedback, structural comments

#### 3. **Anchor Text** (Context-Aware)
```json
{
  "anchorText": "This is the important sentence"
}
```
- **Use Case**: Comments that should "stick" to specific content
- **How it works**: References specific text phrases within the document
- **Validation**: Ensures anchor text exists in document (case-insensitive)
- **Best for**: Content that might move but text stays the same

#### 4. **Line Number** (Code/Structured Documents)
```json
{
  "lineNumber": 42
}
```
- **Use Case**: Code files, structured documents
- **How it works**: References specific line numbers
- **Best for**: Code reviews, structured content

### Combining Strategies
You can combine multiple location strategies for more robust positioning:
```json
{
  "startChar": 150,
  "endChar": 200,
  "paragraphIndex": 2,
  "anchorText": "important sentence"
}
```

### Validation Rules
- At least one location reference must be provided
- Character positions must be within document bounds
- Paragraph indices must exist in the document
- Anchor text must be found in the document
- Start character must be ≤ end character

### Why This Approach?
1. **Flexibility**: Supports different types of documents (prose, code, structured)
2. **Robustness**: Multiple strategies provide fallback options
3. **Precision**: Can be as precise or as general as needed
4. **Extensible**: Easy to add new location strategies
5. **Validation**: Built-in validation ensures location references are valid


## 📦 API Endpoints

### Documents
- `GET /api/documents` - Get all documents
- `POST /api/documents` - Create new document
- `GET /api/documents/{id}` - Get document by ID
- `PUT /api/documents/{id}` - Update document
- `DELETE /api/documents/{id}` - Delete document

### Comments
- `GET /api/documents/{id}/comments` - Get all comments for document
- `POST /api/documents/{id}/comments` - Add comment to document


## 🧪 Sample API Calls

### Create a Document
```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Document",
    "content": "This is the first paragraph.\n\nThis is the second paragraph with important information.\n\nThis is the third paragraph."
  }'
```

### Add Character-Based Comment
```bash
curl -X POST http://localhost:8080/api/documents/{document-id}/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This section needs clarification",
    "author": "John Doe",
    "location": {
      "startChar": 50,
      "endChar": 100
    }
  }'
```

### Add Paragraph-Based Comment
```bash
curl -X POST http://localhost:8080/api/documents/{document-id}/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Great point in this paragraph!",
    "author": "Jane Smith",
    "location": {
      "paragraphIndex": 1
    }
  }'
```

### Add Anchor Text Comment
```bash
curl -X POST http://localhost:8080/api/documents/{document-id}/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "This phrase is key to understanding",
    "author": "Bob Wilson",
    "location": {
      "anchorText": "important information"
    }
  }'
```

### Get All Comments for Document
```bash
curl -X GET http://localhost:8080/api/documents/{document-id}/comments
```

### Get Comments with Filters
```bash
# Filter by author
curl -X GET http://localhost:8080/api/documents/{document-id}/comments?author=John%20Doe

# Filter by paragraph
curl -X GET http://localhost:8080/api/documents/{document-id}/comments?paragraphIndex=1
```

## 🧪 Postman API Testing
- Use the json file and import in postman to test the endpoints [document-commenting.postman_collection.json](postman/document-commenting.postman_collection.json)

### Nice to Have
- Tests (manual, load/stress, penetration, unit, integration)
- Containerize (add containerisation for backend service and the database)
- Add Documentation for methods more then OpenAPI
- Add Liquibase support
- Add Logging support
- ETC
