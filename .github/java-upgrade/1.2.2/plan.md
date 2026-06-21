# Consolidated Plan - Release 1.2.2

- **Project**: `com.microfocus.adm.performancecenter:plugins-common:1.2.2`
- **Date**: 2026-06-21
- **Scope**: Consolidated dependency/security/release plan for version `1.2.2`
- **Supersedes**:
  - `.github/java-upgrade/sec-2025-48924/plan.md`

## Objectives

1. Keep `1.2.2` dependency baseline aligned with approved versions.
2. Ensure CVE remediation is included in the released version.
3. Keep integration-test behavior safe for external builds (skip when local config is missing/placeholder).
4. Produce auditable release artifacts (build and dependency logs).

## Planned changes included in 1.2.2

### Dependency baseline

- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` -> `2.22.0`
- `com.fasterxml.jackson.core:jackson-databind` -> `2.22.0`
- `org.apache.commons:commons-lang3` -> `3.20.0` (test scope)

### Security carry-forward

- Keep CVE-2025-48924 remediation in the release line:
  - legacy `commons-lang:2.6` removed
  - `commons-lang3` used in tests only

### Testing/release evidence

- Generate and store:
  - `.github/java-upgrade/release-1.2.2/build.log`
  - `.github/java-upgrade/release-1.2.2/deps.txt`
  - `.github/java-upgrade/release-1.2.2/deps-full.txt`

## Acceptance criteria

- `mvn clean test` succeeds.
- Targeted dependency tree shows Jackson `2.22.0` and `commons-lang3` test scope.
- Consolidated release summary is present (`.github/java-upgrade/summary.md`).

