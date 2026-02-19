#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building generator-cli..."
mvn -f "$PROJECT_DIR/pom.xml" -pl generator-cli -am package -q -DskipTests

echo "Running generator-cli with realworld spec..."
java -jar "$PROJECT_DIR/spring-oas-generator-cli/target/spring-oas-generator-cli-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/realworld.yml" \
  "$PROJECT_DIR/example-validation/realworld-validation/src/main/java" \
  "nl.stijlaartit.realworld.generated"
