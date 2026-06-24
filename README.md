# performance-center-plugins-common

Shared Java library for plugins that integrate with Opentext enterprise Performance Engineering / Performance Center through REST APIs.

## What this repository provides

- A reusable REST client implementation (`PcRestProxy`) for enterprise Performance Engineering operations.
- Common entities for requests/responses (runs, scripts, tests, trend reports, etc.).
- YAML/XML-to-entity conversion helpers for creating or updating tests.
- Unit tests for parsing, conversion, and request behavior.
- Integration tests for live-server workflows (upload, run lifecycle, start-failure scenarios).

## Availability and distribution

The library is published to Maven Central under the coordinates `com.microfocus.adm.performancecenter:plugins-common`.

- **Sonatype Central (source of truth):** https://central.sonatype.com/artifact/com.microfocus.adm.performancecenter/plugins-common
  - This is the authoritative listing and reflects the latest published versions.
- **MVNRepository (mirror):** https://mvnrepository.com/artifact/com.microfocus.adm.performancecenter/plugins-common
  - This is a third-party mirror that indexes Maven Central. It can lag behind and may not be in sync (for example, a recently published version such as `1.2.2` or `1.2.3` may not yet appear here). For the current state always refer to Sonatype Central.

### Maven dependency

```xml
<dependency>
    <groupId>com.microfocus.adm.performancecenter</groupId>
    <artifactId>plugins-common</artifactId>
    <version>1.2.4</version>
</dependency>
```

### Gradle dependency

```groovy
implementation 'com.microfocus.adm.performancecenter:plugins-common:1.2.4'
```

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

### Transport security (HTTP vs HTTPS)

The transport protocol is supplied by the caller when constructing `PcRestProxy`
(the `webProtocolName` argument, typically `"https"` or `"http"`).

- **HTTPS is recommended for all real and production environments.** Credentials
  (Basic authentication and token-based authentication) and session cookies are only
  protected in transit when HTTPS is used.
- **HTTP is allowed but not recommended.** It is supported on purpose so that customers
  can evaluate OpenText Enterprise Performance Engineering (LoadRunner Enterprise) in
  development/test environments that do not have an SSL certificate configured. Over plain
  HTTP, credentials and cookies are transmitted unencrypted; do not use HTTP outside of
  isolated, non-production environments.

### Full interface reference

Every method declared in `PcRestProxyClient` is documented below. Methods marked
`@Deprecated` are retained for backward compatibility; prefer the lower-case named
replacements.

#### Authentication / session

- `boolean authenticate(String userName, String password)`
  - Opens an authenticated session. When the proxy is configured for token authentication,
    `userName`/`password` are the client id key and secret key; otherwise they are the ALM
    user name and password. Returns `true` on success.
- `boolean logout()`
  - Closes the authenticated session. Returns `true` on success.
- `String GetPcServer()`
  - Returns the resolved enterprise Performance Engineering server (host) the proxy targets.
- `String GetTenant()`
  - Returns the resolved tenant suffix associated with the server, or an empty string when
    no tenant is present.

#### Run lifecycle

- `PcRunResponse startRun(int testId, int testInstanceId, TimeslotDuration timeslotDuration, String postRunAction, boolean vudsMode, int timeslot)`
  - Starts a performance run for the given test and test instance, using the supplied
    timeslot duration, post-run action, VUDs mode, and timeslot id. Returns the created run
    (including its id and state).
- `boolean stopRun(int runId, String stopMode)`
  - Stops the run identified by `runId` using the given stop mode (for example a graceful or
    immediate stop). Returns `true` on success.
- `PcRunResponse getRunData(int runId)`
  - Returns the current data/state for the specified run.

#### Run artifacts and logs

- `PcRunResults getRunResults(int runId)`
  - Returns the collection of result artifacts available for the run.
- `boolean GetRunResultData(int runId, int resultId, String localFilePath)`
  - Downloads a single run result (identified by `resultId`) to `localFilePath`. Returns
    `true` on success.
- `PcRunEventLog getRunEventLog(int runId)`
  - Returns the event log records produced during the run.

#### Script management

- `int uploadScript(String testFolderPath, boolean overwrite, boolean runtimeOnly, boolean keepCheckedOut, String scriptPath)`
  - Uploads the script at `scriptPath` into the test plan folder `testFolderPath`. Flags
    control overwrite behavior, runtime-only upload, and whether the script is kept checked
    out. Returns the new script id.
- `PcScripts getScripts()`
  - Returns all scripts visible in the current project.
- `PcScript getScript(int Id)`
  - Returns the script identified by its numeric id.
- `PcScript getScript(String testFolderPath, String scriptName)`
  - Returns the script located by folder path and script name.
- `boolean deleteScript(int scriptId)`
  - Deletes the script identified by `scriptId`. Returns `true` on success.

#### Test plan folders

- `PcTestPlanFolders getTestPlanFolders()`
  - Returns the test plan folder structure for the project.
- `boolean verifyTestPlanFolderExist(String path)`
  - Returns `true` if the test plan folder at `path` exists.
- `PcTestPlanFolder createTestPlanFolder(String existingPath, String name)`
  - Creates a folder named `name` under the already existing folder `existingPath` and
    returns it.
- `ArrayList<PcTestPlanFolder> createTestPlanFolders(String[] paths)`
  - Creates all folders required by the supplied full `paths`, creating intermediate folders
    as needed, and returns the created folders.
- `ArrayList<PcTestPlanFolder> createPcTestPlanFolders(ArrayList<String[]> stringsOfExistingPathFromSubjectAndOfFolderToCreate)`
  - Lower-level variant of `createTestPlanFolders` that accepts, for each folder, a
    `[existingParentPath, folderNameToCreate]` pair. Returns the created folders.

#### Performance test definitions

- `Test createOrUpdateTestFromYamlTest(String testString)`
  - Creates or updates a test from a full YAML test definition (name, folder, and content in
    one document). Returns the resulting test.
- `Test createOrUpdateTestFromYamlContent(String testName, String testFolderPath, String testOrContent)`
  - Creates or updates the test named `testName` under `testFolderPath` from a YAML content
    (or test) document. Returns the resulting test.
- `Test createOrUpdateTest(String testName, String testFolderPath, String xml)`
  - Creates or updates the test named `testName` under `testFolderPath` from an XML content
    document. Returns the resulting test.
- `Test createOrUpdateTest(String testName, String testFolderPath, Content content)`
  - Creates or updates the test named `testName` under `testFolderPath` from a `Content`
    entity. Returns the resulting test.
- `Test getTest(int testId)`
  - Returns the full test definition for `testId`.
- `PcTest getTestData(int testId)`
  - Returns metadata for the test identified by `testId`.
- `Test updateTest(int testId, Content content)`
  - Replaces the content of an existing test identified by `testId`. Returns the updated test.
- `boolean deleteTest(int testId)`
  - Deletes the test identified by `testId`. Returns `true` on success.

#### Test sets, test instances and timeslots

- `PcTestSets getAllTestSets()`
  - Returns all test sets in the project.
- `PcTestSets GetAllTestSets()` *(deprecated)*
  - Deprecated alias for `getAllTestSets()`.
- `PcTestInstances getTestInstancesByTestId(int testId)`
  - Returns the test instances associated with `testId`.
- `int createTestInstance(int testId, int testSetId)`
  - Creates a test instance linking `testId` to the test set `testSetId`. Returns the new
    test instance id.
- `PcTestSetFolders getTestSetFolders()`
  - Returns the test set folder structure for the project.
- `PcTestSetFolder createTestSetFolder(int parentId, String name)`
  - Creates a test set folder named `name` under the folder `parentId` and returns it.
- `PcTestSet createTestSet(String testSetName, int testSetParentId, String testSetComment)`
  - Creates a test set named `testSetName` under `testSetParentId` with the supplied comment
    and returns it.
- `PcTestSet createTestSet(String testSetName, int testSetParentId)` *(default)*
  - Convenience overload that creates a test set with an empty comment.
- `Timeslots GetOpenTimeslotsByTestId(int testId)`
  - Returns the open timeslots available for `testId`.

#### Trend reporting

- `int addTrendReport(String name, String description)`
  - Creates a trend report with the given name and description. Returns the new trend report
    id.
- `int addTrendReport(String name)` *(default)*
  - Convenience overload that creates a trend report with an empty description.
- `TrendReportTransactionDataRoot getTrendReportByXML(String trendReportId, int runId)`
  - Returns the transaction-level trend report data for the given trend report and run.
- `boolean updateTrendReport(String trendReportId, TrendReportRequest trendReportRequest)`
  - Updates the trend report identified by `trendReportId` with the supplied request. Returns
    `true` on success.
- `InputStream getTrendingPDF(String trendReportId)`
  - Returns the trend report rendered as a PDF stream. The caller is responsible for closing
    the stream.
- `ArrayList<PcTrendedRun> getTrendReportMetaData(String trendReportId)`
  - Returns metadata describing the runs included in the trend report.

#### Helpers

- `int extractTestIdFromString(String value)`
  - Extracts a test id encoded inside a string (for example `ID:'123'`). Returns `0` when no
    id is found.
- `Content readYaml(String yamlContent)`
  - Parses a YAML content document into a `Content` entity.

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

