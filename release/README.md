# Release Process (Sonatype Central)

This repository uses a GitHub Actions workflow to build and deploy releases to Sonatype Central:

- Workflow file: `.github/workflows/release-deploy.yml`
- Trigger file: `release/deploy.txt`

## One-time setup (GitHub repository secrets)

Add these secrets in **Settings → Secrets and variables → Actions**:

| Secret | Description |
| --- | --- |
| `CENTRAL_TOKEN_USERNAME` | Sonatype Central user token name (from `central.sonatype.com` → Account → Generate User Token). |
| `CENTRAL_TOKEN_PASSWORD` | Sonatype Central user token password. |
| `MAVEN_GPG_PRIVATE_KEY_BASE64` | Your ASCII-armored GPG private key, **base64-encoded as a single line** (see below). |
| `MAVEN_GPG_PASSPHRASE` | Passphrase for the GPG private key. |

The workflow configures Maven `serverId=central` and signs artifacts with GPG.

### Why base64 for the GPG key?

GitHub Secrets does **not** reliably preserve the newlines in a multi-line
ASCII-armored PGP key. When pasted directly, the line breaks get collapsed and
`gpg --import` fails with `no valid OpenPGP data found` (exit code 2). Encoding
the key as a single base64 line avoids this entirely; the workflow strips any
whitespace and decodes it back to the original key before importing.

### Generating `MAVEN_GPG_PRIVATE_KEY_BASE64`

First find your signing key ID:

```bash
gpg --list-secret-keys --keyid-format=long
# e.g. sec rsa4096/0E23305B9C989C9A ...  ->  key id is 0E23305B9C989C9A
```

Then export it and base64-encode it as a single line.

**Linux / macOS:**

```bash
gpg --armor --export-secret-keys 0E23305B9C989C9A | base64 -w0 > gpg_key_b64.txt
```

**Windows (PowerShell):**

```powershell
$key = gpg --armor --export-secret-keys 0E23305B9C989C9A
$bytes = [Text.Encoding]::ASCII.GetBytes(($key -join "`n") + "`n")
[Convert]::ToBase64String($bytes) | Set-Content -NoNewline gpg_key_b64.txt
```

Paste the contents of `gpg_key_b64.txt` into the `MAVEN_GPG_PRIVATE_KEY_BASE64`
secret, then **delete the file** (it contains your private key).

You can sanity-check the value decodes back to a valid key:

```bash
base64 -d gpg_key_b64.txt | gpg --show-keys
```

## How to publish a release

1. Update the project version in `pom.xml` (for example `1.2.3`).
2. Edit `release/deploy.txt`:
   - `enabled=true`
   - `version=<same version as pom.xml>`
3. Commit and push to `master`.
4. The workflow will run two jobs:
   - **prepare** – parses `release/deploy.txt`; only proceeds if `enabled=true`.
   - **deploy** – sets up JDK 21 + Maven, imports the GPG key, validates the
     version (`deploy.txt` vs `pom.xml`), then runs
     `mvn -B -ntp clean deploy -DskipITs=true`.
5. Maven signs the artifacts and the `central-publishing-maven-plugin` uploads
   and auto-publishes the bundle to Sonatype Central.

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
Note: the `deploy` job only runs on `master`, so a manual run from any other
branch will not publish anything.

## After a successful release

Set `enabled=false` in `release/deploy.txt` and commit it, to avoid accidental re-deploy on future edits.

## Troubleshooting

**`gpg: no valid OpenPGP data found` / `gpg failed with exit code 2`**
The `MAVEN_GPG_PRIVATE_KEY_BASE64` secret is missing, truncated, or wasn't
base64-encoded as described above. Regenerate it with `base64 -w0` (single line,
no wrapping) and re-paste. Do **not** paste the raw ASCII-armored key — its
newlines get lost in GitHub Secrets.

**`Version mismatch`**
`version=` in `release/deploy.txt` must exactly match `<version>` in `pom.xml`.

**Deployment doesn't start**
The workflow only triggers on pushes that change `release/deploy.txt` on
`master`. Make sure that file is part of your commit. Releases can **only** be
published from `master` — a manual **Run workflow** from any other branch will
run `prepare` but skip the `deploy` job by design.

## Notes

- `mvnrepository.com` may lag behind Sonatype Central indexing.
- The release workflow is intentionally independent from the regular CI build
  workflow (`build.yml` ignores changes to `release/deploy.txt`).
