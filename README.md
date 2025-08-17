# Collaborative Document Commenting Backend

A complete Spring Boot backend system that allows users to create documents and add location-specific comments.

## 🛠 Tech Stack
- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.4
- **ORM**: Hibernate / JPA
- **Database**: PostgresSQL 15
- **Database Migration**: Flyway
- **API Docs**: Swagger
- **Build Tool**: Gradle
- **Java Version**: 21

## 📁 Project Structure

```
design/
├── Application-Design.png
docker/
├── Dockerfile
├── docker-compose.yml
docs/
├── FLYWAY_MIGRATIONS.md
├── HELP.md
kubernates/
├── deployment.yaml
├── service.yaml
postman/
├── document-commenting.postman_collection.json
src/main/kotlin/am/hhovhann/document_comment_service/
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
│   └── DocumentBlock.kt
├── repository/
│   ├── DocumentRepository.kt
│   └── CommentRepository.kt
├── dto/
    ├── location/
    │   ├── AnchorLocation.kt
    │   ├── BlockIdLocation.kt
    │   ├── CharRangeLocation.kt
    │   ├── CommentLocationStrategy.kt
    │   ├── CompositeLocation.kt
    │   └── LineLocation.kt
    │   └── ParagraphLocation.kt
│   ├── DocumentDto.kt
│   └── CommentDto.kt
└── exception/
    ├── Exceptions.kt
    └── GlobalExceptionHandler.kt
src/test/kotlin/am/hhovhann/document_comment_service/
├── DocumentCommentServiceApplicationTest.kt
├── controller/
│   ├── CommentControllerTest.kt
│   └── DocumentControllerTest.kt
├── service/
    ├── integration/
    │   ├── CommentServiceIntegrationTest.kt
    │   ├── DocumentServiceIntegrationTest.kt
    ├── unit/
    │   ├── CommentServiceTest.kt
    │   ├── DocumentServiceTest.kt
```

## 🚀 Setup Instructions

### Prerequisites
- Java 21 or higher
- PostgreSQL 12 or higher
- Gradle 8.0 or higher


### Database Setup
1. **Start PostgreSQL with Docker**
   ```bash
   cd docker && docker-compose up --build
   ```

2. **Database Migrations**
   - Flyway automatically runs migrations on application startup
   - Migrations are located in `src/main/resources/db/migration/`
   - See [Flyway Migration Documentation](docs/FLYWAY_MIGRATIONS.md) for details

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
[Download Docker Desktop](https://docs.docker.com/desktop/)

# Run docker compose locally
```docker-compose up --build```

# Run kubernates (locally need to install minicube)
```kubectl apply -f kubernetes/```

### API Documentation
Once running, access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## 📍 Comment-to-Document Location Mapping

### Design Decision
I chose a **flexible multi-strategy approach** using a `CommentLocationStrategy` interface that supports multiple strategies reference types. This allows comments to be anchored to specific places in documents using different strategies based on use case.

### Location Strategies

#### 1. **Character-Based Positioning** (Most Precise)
```json
{
  "type": "charRange",
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
  "type": "paragraph",
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
  "type": "anchor",
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
  "type": "line",
  "lineNumber": 42
}
```
- **Use Case**: Code files, structured documents
- **How it works**: References specific line numbers
- **Best for**: Code reviews, structured content


#### 5. **Block ID** (Code/Structured Documents) TODO New Strategy BlockIdLocation, NOT SUPPORTED IN THIS VERSION
```json
{
  "type": "block",
  "blockId": "fig-3a"
}
```
- **Use Case**: When documents are not just plain text but contain structured blocks such as:
  - Figures (fig-3a)
  - Tables (tbl-2)
  - Footnotes or citations (note-12)
  - Headings / sections (sec-intro)
  - Embedded media blocks (images, diagrams)
Instead of calculating positions (chars/lines), we reference the logical block in the document model.
- **How it works**: 
  - The document parsing/authoring system assigns stable IDs to blocks.
  - A comment references the block by blockId.
  - The UI can highlight or anchor the comment directly to that block, no matter if text around it changes.
- **Best for**:
  - Rich documents (scientific papers, legal contracts, technical docs)
  - Code+Docs hybrids (Jupyter notebooks, Markdown with figures)
  - CMS/Editor use cases (where blocks are first-class objects)
- **Validation**:
  - Ensure blockId is non-blank. 
  - Optionally, check that the document actually contains a block with that ID (depends if you have a block model in DB).

### Composite Strategies
You can combine multiple location strategies for more robust positioning:
```json
{
  "type": "composite",
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
- Block ID must be found in the document blocks

### Why This Approach?
1. **Flexibility**: Supports different types of documents (prose, code, structured)
2. **Robustness**: Multiple strategies provide fallback options
3. **Precision**: Can be as precise or as general as needed
4. **Extensible**: Easy to add new location strategies
5. **Validation**: Built-in validation in strategies ensures location references are valid


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
      "type": "charRange",
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
      "type": "paragraph",
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
      "type": "anchor",
      "anchorText": "important information"
    }
  }'
```

### Get All Comments for Document
```bash
curl -X GET http://localhost:8080/api/documents/{document-id}/comments
```

## 🧪 Testing

The project has two types of tests: **unit tests** (fast, mocked) and **integration tests** (real DB).  
Both are important to cover business logic and persistence behavior.

---

### ✅ Unit Tests
- Use **mocked repositories** (no database needed).
- Provide fast feedback for service logic and edge cases.

---

### 🔗 Integration Tests
- Run against a **real PostgreSQL database**.
- Validate persistence, optimistic locking, and concurrency behavior.
- Require **Docker** to be running with the test database defined in `docker-compose.yml`.
- The application automatically connects using `application-test.properties`.

Start the database with:

```bash
cd docker
docker-compose up --build -d
```

## 🧪 Postman API Testing
- Use the JSON file and import in POSTMAN to test the endpoints [document-commenting.postman_collection.json](postman/document-commenting.postman_collection.json)

### Nice to Have
- ✅ Database migration support — Added Flyway for versioned schema migrations.
- ✅ Containerization support — Added Docker for database setup and environment parity.
- Document storage improvements
  - Store documents in object storage (e.g., S3, MinIO)
  - Store document metadata and object storage link in the database
- Real-time collaboration backend
  - Replace or supplement the REST with WebSockets
  - Implement Operational Transform (OT) or Conflict-free Replicated Data Types (CRDT) for concurrency-safe edits
- Scalability
  - Architect for high load, multi-document concurrency, and horizontal scaling 
- Testing strategy
  - Load & stress tests
  - Penetration tests
