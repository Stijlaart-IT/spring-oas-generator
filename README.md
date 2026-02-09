# Spring Boot OpenAPI Generator

This project is a Maven multi-module build for OpenAPI-related source generation.

## Modules

- **spring-boot-openapi-generator-generation-base**
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

## Example Validation

The `example-validation/petstore-validation` module contains generated sources from the Petstore OpenAPI spec and tests that validate the generated code. JSON serialization tests verify that each generated model can be serialized to JSON and deserialized back, yielding an equal object. This ensures the generated code is stable and functions as expected.

To run the validation tests:

```bash
mvn -f example-validation/pom.xml test
```

## Dependency version management

Dependency versions are managed centrally in the root `pom.xml` using the `<properties>` and `<dependencyManagement>` sections. Child modules declare dependencies without explicit versions.
