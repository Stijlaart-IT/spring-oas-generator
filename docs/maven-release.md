# Release to Maven Central

Releases are published through GitHub Actions via `.github/workflows/release.yml`.

## Trigger

Publishing runs when a GitHub Release is created (`release.created`).

## Required GitHub Secrets

- `CENTRAL_TOKEN_USERNAME`
- `CENTRAL_TOKEN_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY` (ASCII-armored private key)
- `MAVEN_GPG_PASSPHRASE`

## Release Flow

1. Create a Git tag for the release version (for example: `0.1.0`).
2. Create a GitHub Release for that tag.
3. The workflow sets the Maven version from the release tag:
   - `mvn versions:set -DnewVersion=${{ github.event.release.tag_name }}`
4. The workflow runs `mvn -B -ntp verify`.
5. The workflow runs `mvn -B -ntp -Prelease clean deploy` and publishes artifacts to Maven Central.
