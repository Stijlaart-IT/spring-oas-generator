#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building project..."
mvn -f "$PROJECT_DIR/pom.xml" package -DskipTests

echo "Generating petstore sources..."
java -jar "$PROJECT_DIR/generator-client/target/spring-boot-openapi-generator-client-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/petstore.json" \
  "$PROJECT_DIR/example-validation/petstore-validation-restclient/src/main/java" \
  "nl.stijlaartit.petstore.generated"

echo "Generating realworld sources..."
java -jar "$PROJECT_DIR/generator-client/target/spring-boot-openapi-generator-client-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/realworld.yml" \
  "$PROJECT_DIR/example-validation/realworld-validation-restclient/src/main/java" \
  "nl.stijlaartit.realworld.generated"

echo "Generating spotify sources..."
java -jar "$PROJECT_DIR/generator-client/target/spring-boot-openapi-generator-client-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/spotify.yml" \
  "$PROJECT_DIR/example-validation/spotify-validation-restclient/src/main/java" \
  "nl.stijlaartit.spotify.generated"

echo "Validating generated sources..."
mvn -f "$PROJECT_DIR/example-validation/pom.xml" test

echo "Verification complete."
