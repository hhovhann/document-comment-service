# Flyway Database Migrations

This document describes the Flyway database migration system used in the Document Commenting Service.

## Overview

Flyway is used to manage database schema changes in a version-controlled manner. All database changes are stored as SQL migration files in the `src/main/resources/db/migration` directory.

## Migration Files

### Naming Convention

Migration files follow the naming pattern: `V{version}__{description}.sql`

- `V1__Create_initial_schema.sql` - Creates the base schema
- `V2__Add_optimistic_locking_indexes.sql` - Adds performance indexes

### Migration Locations

- **Production/Development**: `src/main/resources/db/migration/`
- **Testing**: `src/test/resources/db/migration/`

## Current Migrations

### V1__Create_initial_schema.sql
Creates the initial database schema:
- `documents` table with optimistic locking support
- `comments` table with location tracking
- Basic indexes for performance
- PostgreSQL trigger for automatic `updated_at` updates

### V2__Add_optimistic_locking_indexes.sql
Adds performance optimization indexes:
- Version index for optimistic locking queries
- Composite indexes for document search
- Location-based indexes for comments

## Configuration

### Application Properties

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    validate-on-migrate: true
```

### JPA Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Let Flyway handle schema changes
```

## Running Migrations

### Automatic Migration
Migrations run automatically when the application starts.

### Manual Migration
To run migrations manually:

```bash
# Using Flyway CLI
flyway -url=jdbc:postgresql://localhost:5432/document_comments \
       -user=postgres \
       -password=secret \
       migrate

# Using Spring Boot
./gradlew bootRun
```

## Creating New Migrations

1. Create a new SQL file in `src/main/resources/db/migration/`
2. Follow the naming convention: `V{next_version}__{description}.sql`
3. Create a corresponding H2-compatible version in `src/test/resources/db/migration/`
4. Test the migration with the test suite

### Example Migration

```sql
-- V3__Add_user_audit_columns.sql
ALTER TABLE documents ADD COLUMN created_by VARCHAR(100);
ALTER TABLE documents ADD COLUMN updated_by VARCHAR(100);
CREATE INDEX idx_documents_created_by ON documents(created_by);
```

## Testing Migrations

The test environment uses H2 database with Flyway migrations:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  flyway:
    enabled: true
    locations: classpath:db/migration
```

## Best Practices

1. **Never modify existing migrations** - Create new ones instead
2. **Test migrations thoroughly** - Use both PostgreSQL and H2
3. **Keep migrations small and focused** - One logical change per migration
4. **Use descriptive names** - Make the purpose clear
5. **Include rollback considerations** - Plan for potential rollbacks
6. **Validate migrations** - Ensure they work in both environments

## Troubleshooting

### Common Issues

1. **Migration conflicts**: Ensure version numbers are unique and sequential
2. **H2 compatibility**: Some PostgreSQL features may not work in H2
3. **Index creation**: Be careful with index names and syntax differences

### Validation

Flyway validates migrations on startup. Common validation errors:
- Checksum mismatches
- Missing migrations
- Applied migrations not found

### Recovery

If migrations fail:
1. Check the Flyway schema history table
2. Manually fix the database state
3. Mark migrations as resolved if needed
4. Restart the application

## Migration History

The `flyway_schema_history` table tracks all applied migrations:
- `version`: Migration version
- `description`: Migration description
- `type`: Migration type (SQL, etc.)
- `installed_on`: When the migration was applied
- `success`: Whether the migration succeeded
