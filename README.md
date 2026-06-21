# performance-center-plugins-common

Shared Java library for plugins that integrate with Opentext enterprise Performance Engineering / Performance Center through REST APIs.

## What this repository provides

- A reusable REST client implementation (`PcRestProxy`) for enterprise Performance Engineering operations.
- Common entities for requests/responses (runs, scripts, tests, trend reports, etc.).
- YAML/XML-to-entity conversion helpers for creating or updating tests.
- Unit tests for parsing, conversion, and request behavior.
- Integration tests for live-server workflows (upload, run lifecycle, start-failure scenarios).

## Main client interface

The main API contract is `PcRestProxyClient` in:

`src/main/java/com/microfocus/adm/performancecenter/plugins/common/rest/PcRestProxyClient.java`

### Basic method groups (high level)

- **Authentication/session**
  - `authenticate(...)`, `logout()`
  - Opens and closes authenticated sessions.

- **Run lifecycle**
  - `startRun(...)`, `stopRun(...)`, `getRunData(...)`
  - Starts runs, stops runs, and polls run state.

- **Run artifacts and logs**
  - `getRunResults(...)`, `GetRunResultData(...)`, `getRunEventLog(...)`
  - Retrieves result files and event log records.

- **Script management**
  - `uploadScript(...)`, `getScript(...)`, `getScripts()`, `deleteScript(...)`
  - Uploads scripts and manages script inventory.

- **Test plan folders**
  - `getTestPlanFolders()`, `verifyTestPlanFolderExist(...)`, `createTestPlanFolder(...)`, `createTestPlanFolders(...)`
  - Navigates and creates folder structures in the test plan.

- **Performance test definitions**
  - `createOrUpdateTestFromYamlContent(...)`, `createOrUpdateTestFromYamlTest(...)`, `createOrUpdateTest(...)`, `updateTest(...)`, `getTest(...)`, `deleteTest(...)`
  - Creates/updates tests from YAML, XML, or entity objects.

- **Test instances and timeslots**
  - `getAllTestSets()`, `getTestInstancesByTestId(...)`, `createTestInstance(...)`, `GetOpenTimeslotsByTestId(...)`
  - Resolves where and when runs can execute.

- **Trend reporting**
  - `getTrendReportByXML(...)`, `updateTrendReport(...)`, `getTrendingPDF(...)`, `getTrendReportMetaData(...)`
  - Reads and updates trend report artifacts.

- **Helpers**
  - `extractTestIdFromString(...)`, `readYaml(...)`, `GetPcServer()`, `GetTenant()`
  - Utility methods for parsing and context access.

## Test documentation

- Unit tests guide:
  - `src/test/resources/microfocus/adm/performancecenter/plugins/common/rest/UNIT_TESTS_GUIDE.md`
- Integration tests guide:
  - `src/test/resources/microfocus/adm/performancecenter/plugins/common/rest/INTEGRATION_TESTS_GUIDE.md`

## Build and test

```powershell
cd "C:\Git\plugin\performance-center-plugins-common"
mvn test -q
```

For integration test execution and skip behavior, follow the integration guide above.

## Release 1.2.2 dependency baseline

- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.0`
- `com.fasterxml.jackson.core:jackson-databind:2.22.0`
- `org.apache.commons:commons-lang3:3.20.0` (test scope)

Audit artifacts generated for this baseline are under:

- `.github/java-upgrade/release-1.2.2/deps.txt`
- `.github/java-upgrade/release-1.2.2/deps-full.txt`
- `.github/java-upgrade/release-1.2.2/build.log`

