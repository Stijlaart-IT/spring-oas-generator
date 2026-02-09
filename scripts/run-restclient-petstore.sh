#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building generator-client..."
mvn -f "$PROJECT_DIR/pom.xml" -pl generator-client -am package -q -DskipTests

echo "Running generator-client with petstore spec..."
java -jar "$PROJECT_DIR/generator-client/target/generator-client-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/petstore.json" \
  "$PROJECT_DIR/example-validation/petstore-validation-restclient/src/main/java" \
  "nl.stijlaartit.petstore.generated"
