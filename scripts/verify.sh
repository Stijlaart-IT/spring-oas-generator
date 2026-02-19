#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building project..."
mvn -f "$PROJECT_DIR/pom.xml" verify -q

echo "Removing generated sources..."
rm -rf \
  "$PROJECT_DIR/example-validation/petstore-validation/src/main/java/nl/stijlaartit/petstore/generated" \
  "$PROJECT_DIR/example-validation/realworld-validation/src/main/java/nl/stijlaartit/realworld/generated" \
  "$PROJECT_DIR/example-validation/session-validation/src/main/java/nl/stijlaartit/session/generated" \
  "$PROJECT_DIR/example-validation/inline-request-body-validation/src/main/java/nl/stijlaartit/inlinerequestbody/generated" \
  "$PROJECT_DIR/example-validation/variants-validation/src/main/java/nl/stijlaartit/variants/generated" \
  "$PROJECT_DIR/example-validation/spotify-validation/src/main/java/nl/stijlaartit/spotify/generated" \
  "$PROJECT_DIR/example-validation/pokeapi-validation/src/main/java/nl/stijlaartit/pokeapi/generated" \
  "$PROJECT_DIR/example-validation/petstore-validation-maven-plugin/target/generated-sources/nl/stijlaartit/petstore/generated"

echo "Generating petstore sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/petstore.json" \
  "$PROJECT_DIR/example-validation/petstore-validation/src/main/java" \
  "nl.stijlaartit.petstore.generated"

echo "Generating realworld sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/realworld.yml" \
  "$PROJECT_DIR/example-validation/realworld-validation/src/main/java" \
  "nl.stijlaartit.realworld.generated"

echo "Generating session sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/session.json" \
  "$PROJECT_DIR/example-validation/session-validation/src/main/java" \
  "nl.stijlaartit.session.generated"

echo "Generating inline request body sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/inline-request-body.json" \
  "$PROJECT_DIR/example-validation/inline-request-body-validation/src/main/java" \
  "nl.stijlaartit.inlinerequestbody.generated"

echo "Generating variants sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/variants.yml" \
  "$PROJECT_DIR/example-validation/variants-validation/src/main/java" \
  "nl.stijlaartit.variants.generated"

echo "Generating spotify sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/spotify.yml" \
  "$PROJECT_DIR/example-validation/spotify-validation/src/main/java" \
  "nl.stijlaartit.spotify.generated"

echo "Generating pokeapi sources..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/pokeapi.yml" \
  "$PROJECT_DIR/example-validation/pokeapi-validation/src/main/java" \
  "nl.stijlaartit.pokeapi.generated"

echo "Validating generated sources..."
mvn -f "$PROJECT_DIR/example-validation/pom.xml" test

echo "Verification complete."
