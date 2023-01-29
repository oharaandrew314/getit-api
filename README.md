# GetIt

Shopping List API.

Serves as a reference for my current best practices.

## Modules

### Core

Shopping List business logic, repositories, DTOs, and DTO serializers (via kotlinx.serialization)

### Http4k

The primary RESTful implementation.  Contains a Swagger UI.

### Ktor

Alternate RESTful implementation for the core module.

### Spring-Web

Alternate RESTful implementation for the core module.

### Root

- local runners for http4k and ktor
- Lambda deployment package for the http4k module

## TODO

- Split repositories out of the core module, and make Exposed implementation of the repository
- Make interface module so that implementation details no longer need to be leaked out of core
- Move http4k and Ktor local runners to their own modules

