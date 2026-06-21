# Integration Tests Guide

This document is the single source of truth for integration test setup and execution.

## Purpose

- Unit tests always run and never require a live enterprise Performance Engineering server.
- Integration tests run only when local configuration is explicitly provided.
- Placeholder/example values cause integration tests to be skipped automatically.

## Configuration Model

1. Copy `src/test/resources/integration-tests.properties.template` to `src/test/resources/integration-tests.properties`.
2. Replace all `YOUR_*` and `example` placeholders with real local values.
3. Keep `integration-tests.properties` uncommitted (already ignored by `.gitignore`).

### Required for any integration test

- `pc.lre.server`
- `pc.alm.domain`
- `pc.alm.project`
- Either:
  - `pc.alm.user` and `pc.alm.password`
  - or `pc.lre.idKey` and `pc.lre.secretKey`

### Additional required keys by scenario

- Upload script integration:
  - `pc.test.folderPath`
  - `pc.test.scriptPath`
- Run lifecycle integration:
  - `pc.test.kilimanjaro.scriptPath`
  - `pc.test.kilimanjaro.folderPath`
- Run start-failure integration:
  - `pc.test.kilimanjaro.scriptPath`
  - `pc.test.kilimanjaro.folderPath`

## Execution

### Unit tests only

```powershell
cd "C:\Git\plugin\performance-center-plugins-common"
mvn test -q
```

### Integration tests profile

```powershell
cd "C:\Git\plugin\performance-center-plugins-common"
mvn verify -P internal-it
```

### Targeted integration classes

```powershell
cd "C:\Git\plugin\performance-center-plugins-common"
mvn -P internal-it "-Dit.test=TestRunLifecycleIntegration" verify
mvn -P internal-it "-Dit.test=TestRunStartFailureIntegration" verify
mvn -P internal-it "-Dit.test=TestUploadScriptIntegration" verify
```

## Skip Behavior

Integration tests are skipped when:

- `integration-tests.properties` does not exist, or
- required keys are missing, or
- required keys still contain placeholder/example values.

This protects external/CI environments from trying to run live-server tests with invalid defaults.

## Notes

- Do not store real server URLs or credentials in markdown files.
- Use local `integration-tests.properties` for personal environment values.
- Release `1.2.2` dependency audit artifacts are available at:
  - `.github/java-upgrade/release-1.2.2/deps.txt`
  - `.github/java-upgrade/release-1.2.2/deps-full.txt`

