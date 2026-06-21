# Consolidated Summary - Release 1.2.2

- **Project**: `com.microfocus.adm.performancecenter:plugins-common:1.2.2`
- **Date**: 2026-06-21
- **Purpose**: Single release summary for dependency/security/test evidence.
- **Merged from**:
  - `.github/java-upgrade/sec-2025-48924/summary.md`
  - `.github/java-upgrade/release-1.2.2/summary.md`

## Release dependency baseline

- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.0`
- `com.fasterxml.jackson.core:jackson-databind:2.22.0`
- `org.apache.commons:commons-lang3:3.20.0` (test scope)

## Security status

- CVE-2025-48924 remediation is included in `1.2.2`:
  - vulnerable `commons-lang:commons-lang:2.6` is not present
  - migrated to `org.apache.commons:commons-lang3` in test scope

## Verification evidence

- Build log:
  - `.github/java-upgrade/release-1.2.2/build.log`
- Targeted dependency tree:
  - `.github/java-upgrade/release-1.2.2/deps.txt`
- Full dependency tree:
  - `.github/java-upgrade/release-1.2.2/deps-full.txt`

## Build snapshot

- Command: `mvn clean test`
- Result: `BUILD SUCCESS`
- Unit test result: `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`

## Notes

- Historical per-topic notes are still available in their original folders.
- This file is the canonical summary for release `1.2.2`.

