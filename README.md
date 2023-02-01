# GetIt

Shopping List API.

Serves as a reference for my current best practices.

## Common Environment Variables

- aws_profile
- lists_table_name
- items_table_name
- jwt_audience

## Modules

### Core

Shopping List business logic, repositories, DTOs, and DTO serializers (via kotlinx.serialization)

### Http4k

The primary RESTful implementation.

### Ktor

Alternate RESTful implementation for the core module.

### Spring-Web

Alternate RESTful implementation for the core module.

### Root

- local runners for http4k and ktor
- Lambda deployment package for the http4k module
