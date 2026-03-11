#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building project..."
mvn -f "$PROJECT_DIR/pom.xml" verify -q

echo "Removing generated sources..."
rm -rf \
  "$PROJECT_DIR/example-validation/variants-validation-jackson2/target" \
  "$PROJECT_DIR/example-validation/petstore-validation/src/main/java/nl/stijlaartit/petstore/generated" \
  "$PROJECT_DIR/example-validation/variants-validation-jackson2/src/main/java/nl/stijlaartit/variants/jackson2/generated" \
  "$PROJECT_DIR/example-validation/realworld-validation/src/main/java/nl/stijlaartit/realworld/generated" \
  "$PROJECT_DIR/example-validation/variants-validation/src/main/java/nl/stijlaartit/variants/generated" \
  "$PROJECT_DIR/example-validation/spotify-validation/src/main/java/nl/stijlaartit/spotify/generated" \
  "$PROJECT_DIR/example-validation/pokeapi-validation/src/main/java/nl/stijlaartit/pokeapi/generated" \
  "$PROJECT_DIR/example-validation/spring-config-validation/src/main/java/nl/stijlaartit/springconfig/generated" \
  "$PROJECT_DIR/example-validation/petstore-validation-maven-plugin/target/generated-sources/nl/stijlaartit/petstore/generated"

echo "Generating petstore sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  --openapi-spec "$PROJECT_DIR/examples/petstore.json" \
  --output-path "$PROJECT_DIR/example-validation/petstore-validation/src/main/java" \
  --output-package "nl.stijlaartit.petstore.generated"

echo "Generating realworld sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  --openapi-spec "$PROJECT_DIR/examples/realworld.yml" \
  --output-path "$PROJECT_DIR/example-validation/realworld-validation/src/main/java" \
  --output-package "nl.stijlaartit.realworld.generated"

echo "Generating variants jackson2 sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  --openapi-spec "$PROJECT_DIR/examples/variants.yml" \
  --output-path "$PROJECT_DIR/example-validation/variants-validation-jackson2/src/main/java" \
  --output-package "nl.stijlaartit.variants.jackson2.generated" \
  --record-model-jackson-version 2

echo "Generating variants sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  --openapi-spec "$PROJECT_DIR/examples/variants.yml" \
  --output-path "$PROJECT_DIR/example-validation/variants-validation/src/main/java" \
  --output-package "nl.stijlaartit.variants.generated"

echo "Generating spotify sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  --openapi-spec "$PROJECT_DIR/examples/spotify.yml" \
  --output-path "$PROJECT_DIR/example-validation/spotify-validation/src/main/java" \
  --output-package "nl.stijlaartit.spotify.generated"

echo "Generating pokeapi sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  --openapi-spec "$PROJECT_DIR/examples/pokeapi.yml" \
  --output-path "$PROJECT_DIR/example-validation/pokeapi-validation/src/main/java" \
  --output-package "nl.stijlaartit.pokeapi.generated"

echo "Generating spring-config sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  --openapi-spec "$PROJECT_DIR/examples/petstore.json" \
  --output-path "$PROJECT_DIR/example-validation/spring-config-validation/src/main/java" \
  --output-package "nl.stijlaartit.springconfig.generated" \
  --spring-config-service-group-name "petstore"

echo "Validating generated sources..."
mvn -f "$PROJECT_DIR/example-validation/pom.xml" test

echo "Verification complete."
