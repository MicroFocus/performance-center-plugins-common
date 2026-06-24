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
- Concurrent upload / thread-safety integration:
  - `pc.test.folderPath`
  - `pc.test.scriptPath`
  - `pc.test.concurrentUploads` (optional, default `5`) — number of scripts uploaded in parallel
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
mvn -P internal-it "-Dit.test=TestConcurrentUploadScriptIntegration" verify
mvn -P internal-it "-Dit.test=TestTestSetAndTrendReportIntegration" verify
```

## Thread-safety and concurrent uploads

`PcRestProxy` is safe to use from multiple threads: each request builds its own
`HttpContext` while sharing a single thread-safe cookie store (authenticated session).
This is covered by:

- `PcRestProxyThreadSafetyUnitTest` (unit) — verifies fresh per-request context, shared
  cookie store, and concurrent request correctness without a live server.
- `TestConcurrentUploadScriptIntegration` (integration) — drives concurrent reads on a
  single shared instance and parallel script uploads against a live LRE server.

> **Server constraint:** The LRE REST API serializes script uploads **per session**. Two
> simultaneous `POST /Scripts` uploads on the *same* authenticated session yield one
> `201 Created` and one `400` (error code 1600), even though both are well-formed multipart
> requests. To upload several scripts at once, give each worker thread its own session
> (one `PcRestProxy` + one `authenticate(...)` per thread). Concurrent **read** operations
> on a single shared instance are fully supported.

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
