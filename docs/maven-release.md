# Release to Maven Central

Releases are published through GitHub Actions via `.github/workflows/release.yml` and `.github/workflows/maven-central-deploy.yml`.

## Trigger

Publishing is handled by `.github/workflows/maven-central-deploy.yml` and is triggered from a Git tag.
Supported tags:

- Release tag: `X.Y.Z`
- Snapshot tag: `X.Y.Z.YYYYMMDDHHMM-SNAPSHOT`

## Required GitHub Secrets

- `CENTRAL_TOKEN_USERNAME`
- `CENTRAL_TOKEN_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY` (ASCII-armored private key)
- `MAVEN_GPG_PASSPHRASE`

## Release Flow

1. Push a release or snapshot tag.
2. The workflow maps the tag to the deploy version:
   - `X.Y.Z` -> `X.Y.Z`
   - `X.Y.Z.YYYYMMDDHHMM-SNAPSHOT` -> `X.Y.Z-SNAPSHOT`
3. The workflow sets the Maven version from the resolved deploy version.
4. The workflow runs `mvn -B -ntp verify`.
5. The workflow publishes with `-Prelease clean deploy`, selecting:
   - `central` for non-snapshot releases
   - `central-snapshots` for snapshot releases
