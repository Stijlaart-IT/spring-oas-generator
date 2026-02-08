# Spring Boot OpenAPI Generator

This project is a Maven multi-module build for OpenAPI-related source generation.

## Modules

- **spring-boot-openapi-generator-generator**
  - Depends on `com.squareup:javapoet`.
  - Responsible for generating Java source files.
  - No source implementation is added yet.

- **spring-boot-openapi-generator-model-generation**
  - Depends on `io.swagger.parser.v3:swagger-parser`.
  - Responsible for generating a set of source files based on a set of JSON Schema definitions.
  - No source implementation is added yet.

- **spring-boot-openapi-generator-client-generation**
  - Depends on `io.swagger.parser.v3:swagger-parser`.
  - Responsible for generating a set of source files for a client based on schema/API definitions.
  - No source implementation is added yet.

- **spring-boot-openapi-generator-client-restclient**
  - Placeholder module for RestClient-focused client generation/runtime support.

- **spring-boot-openapi-generator-client-webclient**
  - Placeholder module for WebClient-focused client generation/runtime support.

## Examples

To download the example OpenAPI specs, run:

```bash
./scripts/download-examples.sh
```

This downloads the Petstore OpenAPI spec to `examples/petstore.json`.

## Dependency version management

Dependency versions are managed centrally in the root `pom.xml` using the `<properties>` and `<dependencyManagement>` sections. Child modules declare dependencies without explicit versions.

