#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

mkdir -p "$PROJECT_DIR/examples"
curl -sL https://petstore3.swagger.io/api/v3/openapi.json -o "$PROJECT_DIR/examples/petstore.json"
echo "Downloaded petstore.json to examples/petstore.json"

curl -sL https://raw.githubusercontent.com/realworld-apps/realworld/refs/heads/main/api/openapi.yml -o "$PROJECT_DIR/examples/realworld.yml"
echo "Downloaded realworld.yml to examples/realworld.yml"
