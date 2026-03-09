# Spring OAS Generator

Generate Java client interfaces and model classes from an OpenAPI specification for Spring applications.
The generator supports both OAS 3.1 and OAS 3.0.

This README is for users who want to consume the generator in their own project (not for project contributors).

Generated code is written under your chosen base package:

- `<basePackage>.client`
- `<basePackage>.models`

You can run the generator either:

- As a Maven plugin (`spring-oas-generator-maven-plugin`)
- As a standalone CLI jar (`spring-oas-generator-cli`)

## Design Choices Code Generation

[See design choices](docs/design-choices.md).

## Usage

### Maven Plugin

The goal is bound to `generate-sources` by default. Generated files are written to `${project.build.directory}/generated-sources`, and this directory is automatically added as a compile source root.

Minimal plugin configuration:

```xml
<properties>
    <spring-oas-generator.version>{{version}}</spring-oas-generator.version>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>nl.stijlaart-it.spring-oas-generator</groupId>
            <artifactId>spring-oas-generator-maven-plugin</artifactId>
            <version>${spring-oas-generator.version}</version>
            <executions>
                <execution>
                    <id>generate-openapi</id>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <openapiSpec>${project.basedir}/openapi.yml</openapiSpec>
                        <outputPackage>com.example.generated</outputPackage>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Full plugin configuration (all options):

```xml
<properties>
    <spring-oas-generator.version>{{version}}</spring-oas-generator.version>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>nl.stijlaart-it.spring-oas-generator</groupId>
            <artifactId>spring-oas-generator-maven-plugin</artifactId>
            <version>${spring-oas-generator.version}</version>
            <executions>
                <execution>
                    <id>generate-openapi</id>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <openapiSpec>${project.basedir}/openapi.yml</openapiSpec>
                        <outputPackage>com.example.generated</outputPackage>
                        <recordModel>
                            <builderMode>STRICT</builderMode>
                            <disableJacksonRequired>false</disableJacksonRequired>
                            <jacksonVersion>3</jacksonVersion>
                        </recordModel>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Run with top-level required properties via Maven CLI (`-D...`):

```bash
mvn nl.stijlaart-it.spring-oas-generator:spring-oas-generator-maven-plugin:<version>:generate \
  -DopenapiSpec=/absolute/path/to/openapi.yml \
  -DoutputPackage=com.example.generated
```

### Standalone CLI (Jar)

Run pattern:

```bash
java -jar spring-oas-generator-cli/target/spring-oas-generator-cli-<version>.jar \
  --openapi-spec path/to/openapi.yml \
  --output-path path/to/generated/sources/root \
  --output-package com.example.generated
```

See all CLI argument in the Configuration Reference below.

### Generated Source Code Required Dependencies

### Spring Boot 4 / Spring Framework 7 / Jackson 3

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-restclient</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-jackson</artifactId>
    </dependency>
</dependencies>
```

> Generated code uses `org.jspecify.annotations.Nullable`. In Spring Boot 4 projects, this is typically available transitively.

### Spring Boot 3 / Spring Framework 6 / Jackson 2

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    <dependency>
        <groupId>org.jspecify</groupId>
        <artifactId>jspecify</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## Using Generated HTTP Service Clients

Generated API interfaces are placed under `<basePackage>.client`, and generated models under `<basePackage>.models`.
Operations are grouped by OpenAPI tag into `*Api` interfaces; operations without tags are generated in `DefaultApi`.

### 1. Create an API bean

```java
import com.example.generated.client.PetApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ApiClientConfig {

    @Bean
    PetApi petApi() {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.example.com")
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(PetApi.class);
    }
}
```

### 2. Inject and call generated clients

```java
import com.example.generated.client.PetApi;
import com.example.generated.models.Pet;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PetService {

    private final PetApi petApi;

    public PetService(PetApi petApi) {
        this.petApi = petApi;
    }

    public Pet createPet(Pet input) {
        // Generated operation method: direct response type.
        Pet created = petApi.addPet(input);

        // Generated operation method: ResponseEntity variant.
        ResponseEntity<Pet> createdResponse = petApi.addPetResponseEntity(input);

        return createdResponse.getBody() != null ? createdResponse.getBody() : created;
    }
}
```

For each generated operation, two methods are available:

- A direct return method (`T` or `void`)
- A `...ResponseEntity` method (`ResponseEntity<T>` or `ResponseEntity<Void>`)

## Configuration Reference

| Property | Description | Maven Configuration | CLI Flag | Allowed Values |
| --- | --- | --- | --- | --- |
| `openapiSpec` | Path to the input OpenAPI spec file. Must exist, be readable, and end with `.yml`, `.yaml`, or `.json`. No default value (required). Determines which API definition is used for generated sources. | `<openapiSpec>...</openapiSpec>` | `--openapi-spec /path/to/spec.yml` | valid file path |
| `outputPackage` | Base Java package for generated code. No default value (required). Generated files go to `<outputPackage>.client` and `<outputPackage>.models`. | `<outputPackage>com.example.generated</outputPackage>` | `--output-package com.example.generated` | valid Java package name |
| Maven output path / CLI output path | Where generated files are written. Maven default is `${project.build.directory}/generated-sources`; CLI has no default value (required). Maven output dir is auto-added as compile source root; CLI writes to the provided directory. | Maven plugin always writes to `${project.build.directory}/generated-sources`. | `--output-path /path/to/output` | valid directory path |
| `recordModel.builderMode` | Controls record builder generation and strictness. Default is `STRICT`. `DISABLED`: no builder. `STRICT`: builder enforces required non-null fields in `build()`. `RELAXED`: builder generated without required non-null enforcement in `build()`. | `<recordModel><builderMode>STRICT</builderMode></recordModel>` | `--record-model-builder-mode STRICT` | `DISABLED`, `STRICT`, `RELAXED` |
| `recordModel.disableJacksonRequired` | Controls whether `@JsonProperty(required = true)` is emitted for required fields. Default is `false`. `true` disables required=true on generated `@JsonProperty` annotations for required fields. | `<recordModel><disableJacksonRequired>false</disableJacksonRequired></recordModel>` | `--record-model-disable-jackson-required` or `--record-model-disable-jackson-required=true|false` | `true`, `false` |
| `recordModel.jacksonVersion` | Selects Jackson 2 vs Jackson 3 APIs in generated `NullWrapper` code. Default is `3`. `2` generates `com.fasterxml...` imports/APIs. `3` generates `tools.jackson...` imports/APIs. | `<recordModel><jacksonVersion>3</jacksonVersion></recordModel>` | `--record-model-jackson-version 3` | `2`, `3` |

## Development Requirements

- Java: `21`
- Maven: `3.9+` recommended

Build and verify:

```bash
mvn verify
```

Or run the project verification script:

```bash
/Users/matstijl/development/repositories/github/stijlaart-it/spring-boot-openapi-generator/scripts/verify.sh
```

Difference:

- `mvn verify` builds and tests the modules.
- `scripts/verify.sh` also regenerates validation example sources and runs `example-validation` tests.
