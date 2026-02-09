#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "Building client-restclient..."
mvn -f "$PROJECT_DIR/pom.xml" -pl client-restclient -am package -q -DskipTests

echo "Running client-restclient with realworld spec..."
java -jar "$PROJECT_DIR/client-restclient/target/client-restclient-0.0.1-SNAPSHOT.jar" \
  "$PROJECT_DIR/examples/realworld.yml" \
  "$PROJECT_DIR/example-validation/realworld-validation/src/main/java" \
  "nl.stijlaartit.realworld.generated"
