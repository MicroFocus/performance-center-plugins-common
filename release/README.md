# Release Process (Sonatype Central)

This repository uses a GitHub Actions workflow to build and deploy releases to Sonatype Central:

- Workflow file: `.github/workflows/release-deploy.yml`
- Trigger file: `release/deploy.txt`

## One-time setup (GitHub repository secrets)

Add these secrets in **Settings → Secrets and variables → Actions**:

- `CENTRAL_TOKEN_USERNAME`
- `CENTRAL_TOKEN_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY` (ASCII-armored private key content)
- `MAVEN_GPG_PASSPHRASE`

The workflow configures Maven `serverId=central` and signs artifacts with GPG.

## How to publish a release

1. Update the project version in `pom.xml` (for example `1.2.3`).
2. Edit `release/deploy.txt`:
   - `enabled=true`
   - `version=<same version as pom.xml>`
3. Commit and push to `main` or `master`.
4. The workflow will run and execute:
   - version validation (`deploy.txt` vs `pom.xml`)
   - `mvn clean deploy -DskipITs=true`

## Trigger file format

`release/deploy.txt`:

```txt
enabled=true
version=1.2.3
```

If `enabled` is not `true`, deployment is skipped.

## Manual run

You can also run the workflow manually via **Actions → Release Deploy → Run workflow**.
It still reads `release/deploy.txt`, so keep `enabled=true` there for deployment.

## After a successful release

Set `enabled=false` in `release/deploy.txt` and commit it, to avoid accidental re-deploy on future edits.

## Notes

- `mvnrepository.com` may lag behind Sonatype Central indexing.
- The release workflow is intentionally independent from the regular CI build workflow.
