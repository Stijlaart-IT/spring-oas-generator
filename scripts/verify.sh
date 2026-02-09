#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building project..."
mvn -f "$PROJECT_DIR/pom.xml" package -q -DskipTests

echo "Generating petstore sources..."
java -jar "$PROJECT_DIR/client-restclient/target/client-restclient-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/petstore.json" \
  "$PROJECT_DIR/example-validation/petstore-validation/src/main/java" \
  "nl.stijlaartit.petstore.generated"

echo "Generating realworld sources..."
java -jar "$PROJECT_DIR/client-restclient/target/client-restclient-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/realworld.yml" \
  "$PROJECT_DIR/example-validation/realworld-validation/src/main/java" \
  "nl.stijlaartit.realworld.generated"

echo "Validating generated sources..."
mvn -f "$PROJECT_DIR/example-validation/pom.xml" test -q

echo "Verification complete."
