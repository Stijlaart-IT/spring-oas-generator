#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <version X.Y.Z>" >&2
  exit 1
fi

VERSION="$1"

if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Error: version must match X.Y.Z using digits only." >&2
  exit 1
fi

DATETIME="$(date -u +%Y%m%d%H%M)"
TAG="${VERSION}.${DATETIME}-SNAPSHOT"

if git rev-parse -q --verify "refs/tags/${TAG}" >/dev/null; then
  echo "Error: tag '${TAG}' already exists locally." >&2
  exit 1
fi

if git ls-remote --tags origin "refs/tags/${TAG}" | grep -q .; then
  echo "Error: tag '${TAG}' already exists on origin." >&2
  exit 1
fi

git tag "${TAG}"
git push origin "refs/tags/${TAG}"

echo "Created and pushed tag: ${TAG}"
