# Unit Tests Guide

This document describes the unit-test scope in this repository.

## Purpose

- Validate parsing, conversion, and client request behavior without requiring a live enterprise Performance Engineering server.
- Keep fast feedback for local development and CI.
- Ensure unit tests run independently from integration-test configuration.

## Typical unit-test areas

- Request/response handling around `PcRestProxy` request logic.
- YAML/XML conversion and content parsing.
- Scheduler and simplified group/script resolver behavior.

## Execution

```powershell
cd "C:\Git\plugin\performance-center-plugins-common"
mvn test -q
```

## Notes

- Unit tests should not require `integration-tests.properties`.
- Live-server scenarios are covered by integration tests; see:
  - `src/test/resources/microfocus/adm/performancecenter/plugins/common/rest/INTEGRATION_TESTS_GUIDE.md`
- Release `1.2.2` unit-test build evidence is stored at:
  - `.github/java-upgrade/release-1.2.2/build.log`

