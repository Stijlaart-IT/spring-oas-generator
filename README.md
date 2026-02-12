# Spring Boot OpenAPI Generator

This project is a Maven multi-module build for OpenAPI-related source generation.

## Modules

- **generator-engine**
  - Depends on `com.palantir.javapoet:javapoet` and `io.swagger.parser.v3:swagger-parser`.
  - Contains the generation engine, aspects, resolvers, and writers.

- **spring-boot-openapi-generator-cli**
  - CLI wrapper that maps command-line arguments to the generator engine.

- **spring-boot-openapi-generator-client-webclient**
  - Placeholder module for WebClient-focused client generation/runtime support.

## Flow

### Phases:
* OpenAPI parsing: Transform a YML or JSON to a intractable OpenAPI object. This should only yield valid OpenAPI objects, otherwise an exception is thrown.
* Schema Registry: Registry containing all schemas in the OAS with relations to the parent schemas (required for contextual code generation).
* Schema Model: 

### Dependencies
 * OpenAPI parsing -> Schema Registry
 * Schema Registry -> Type Resolver
 * Type Resolver -> Model Resolver
 * Model Resolver -> Model Writer
 * Type Resolver -> Client Resolver
 * Client Resolver -> Client Writer
 
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
