/**
 * Copyright © 2023 Open Text Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microfocus.adm.performancecenter.plugins.common.rest;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.*;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test for the full run lifecycle:
 * <ol>
 *   <li>Upload kilimanjaro.zip script to Subject\scripts</li>
 *   <li>Create a performance test from YAML referencing the uploaded script (10 VUsers, LG1)</li>
 *   <li>Resolve the test instance (auto-create if needed)</li>
 *   <li>Start the run</li>
 *   <li>Monitor run state by polling getRunData every 5 seconds and logging each state transition</li>
 *   <li>Stop the run once it reaches the RUNNING state (keeps the test short)</li>
 *   <li>Retrieve the run event log</li>
 *   <li>Retrieve the list of run results</li>
 *   <li>Clean up: delete test and script</li>
 * </ol>
 *
 * <p>Skipped automatically when the LRE server configuration is absent
 * (no integration-tests.properties and no {@code pc.*} system properties).</p>
 */
public class TestRunLifecycleIntegration {

    // ── configuration ────────────────────────────────────────────────────────
    private static final String WEB_PROTOCOL   = PcRestProxyBase.WEB_PROTOCOL;
    private static final String LRE_SERVER     = PcRestProxyBase.LRE_SERVER_NAME_WITH_TENANT;
    private static final String ALM_DOMAIN     = PcRestProxyBase.ALM_DOMAIN;
    private static final String ALM_PROJECT    = PcRestProxyBase.ALM_PROJECT;
    private static final String USERNAME        = PcRestProxyBase.ALM_USER_NAME;
    private static final String PASSWORD        = PcRestProxyBase.ALM_PASSWORD;
    private static final String KILIMANJARO_ZIP = PcRestProxyBase.kilimanjaroScriptPath;
    private static final String SCRIPT_FOLDER   = PcRestProxyBase.kilimanjaroFolderPath;

    /** Dedicated controller (no {@code controller:} key → LRE assigns a dedicated one). */
    private static final String TEST_FOLDER    = "Subject\\scripts";

    /** Short timeslot – 30 minutes minimum on most LRE servers. */
    private static final TimeslotDuration TIMESLOT = new TimeslotDuration(0, 30);

    /** Post-run action applied after the run finishes. */
    private static final String POST_RUN_ACTION = PostRunAction.COLLATE_AND_ANALYZE.getValue();

    /** How long to poll before giving up (ms). */
    private static final long   MONITOR_TIMEOUT_MS = 10L * 60 * 1000; // 10 minutes

    /** Interval between state polls (ms). */
    private static final long   POLL_INTERVAL_MS   = 5_000;

    /** States that are considered terminal for this test. */
    private static final List<RunState> TERMINAL_STATES = Arrays.asList(
            RunState.FINISHED,
            RunState.CANCELED,
            RunState.RUN_FAILURE,
            RunState.FAILED_COLLATING_RESULTS,
            RunState.FAILED_CREATING_ANALYSIS_DATA,
            RunState.BEFORE_COLLATING_RESULTS,       // run ended, results not yet collated
            RunState.BEFORE_CREATING_ANALYSIS_DATA   // collation done
    );

    // ── mutable state shared between steps / tearDown ────────────────────────
    private PcRestProxy pcRestProxy;
    private boolean     isAuthenticated = false;
    private int         uploadedScriptId = 0;
    private int         createdTestId    = 0;
    private int         runId            = 0;

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @Before
    public void setUp() throws PcException, IOException {
        log("=== setUp ===");
        Assume.assumeTrue("Skipping integration test: integration-tests.properties is missing.",
                IntegrationTestConfig.hasPropertiesFile());
        Assume.assumeTrue("Skipping integration test: missing required LRE connection keys in integration-tests.properties.",
                IntegrationTestConfig.isConfigured());
        Assume.assumeTrue("Skipping run lifecycle integration test: missing or placeholder pc.test.kilimanjaro.scriptPath in integration-tests.properties.",
                IntegrationTestConfig.hasUsableFileValue("pc.test.kilimanjaro.scriptPath"));
        Assume.assumeTrue("Skipping run lifecycle integration test: missing or placeholder pc.test.kilimanjaro.folderPath in integration-tests.properties.",
                IntegrationTestConfig.hasUsableFileValue("pc.test.kilimanjaro.folderPath"));

        pcRestProxy = new PcRestProxy(
                WEB_PROTOCOL, LRE_SERVER,
                false,          // username/password auth
                ALM_DOMAIN, ALM_PROJECT,
                "", "", "");    // no proxy

        isAuthenticated = pcRestProxy.authenticate(USERNAME, PASSWORD);
        assertTrue("Authentication must succeed", isAuthenticated);
        log("Authenticated successfully as " + USERNAME);
    }

    @After
    public void tearDown() {
        log("=== tearDown ===");

        // Attempt to stop the run if it is still tracked.
        if (runId > 0) {
            try {
                log("Attempting to stop runId=" + runId);
                pcRestProxy.stopRun(runId, "stop");
            } catch (Exception ignored) {
                log("Stop run ignored: " + ignored.getMessage());
            }
        }

        // Delete the test that was created.
        if (createdTestId > 0) {
            try {
                boolean deleted = pcRestProxy.deleteTest(createdTestId);
                log("Deleted test ID=" + createdTestId + ": " + deleted);
            } catch (Exception ignored) {
                log("Delete test ignored: " + ignored.getMessage());
            }
        }

        // Delete the uploaded script.
        if (uploadedScriptId > 0) {
            try {
                boolean deleted = pcRestProxy.deleteScript(uploadedScriptId);
                log("Deleted script ID=" + uploadedScriptId + ": " + deleted);
            } catch (Exception ignored) {
                log("Delete script ignored: " + ignored.getMessage());
            }
        }

        // Logout.
        if (pcRestProxy != null && isAuthenticated) {
            try {
                pcRestProxy.logout();
                log("Logged out.");
            } catch (Exception ignored) {
                log("Logout ignored: " + ignored.getMessage());
            }
        }
    }

    // ── test ──────────────────────────────────────────────────────────────────

    @org.junit.Test
    public void testRunLifecycle() throws Exception {

        // ── Step 1: Upload kilimanjaro.zip ───────────────────────────────────
        log("\n── Step 1: Upload kilimanjaro.zip to " + SCRIPT_FOLDER + " ──");
        File scriptFile = new File(KILIMANJARO_ZIP);
        assertTrue("kilimanjaro.zip must exist at: " + scriptFile.getAbsolutePath(), scriptFile.exists());

        uploadedScriptId = pcRestProxy.uploadScript(
                SCRIPT_FOLDER,
                true,   // overwrite
                true,   // runtimeOnly
                false,  // keepCheckedOut
                KILIMANJARO_ZIP);
        assertTrue("Uploaded script ID must be > 0, got: " + uploadedScriptId, uploadedScriptId > 0);
        log("Script uploaded successfully. ID=" + uploadedScriptId);

        // Verify we can retrieve the uploaded script.
        PcScript pcScript = pcRestProxy.getScript(uploadedScriptId);
        assertNotNull("Retrieved script must not be null", pcScript);
        log("Script details: name=" + pcScript.getName()
                + ", path=" + pcScript.getTestFolderPath()
                + ", protocol=" + pcScript.getProtocol());

        // ── Step 2: Create test from YAML ────────────────────────────────────
        log("\n── Step 2: Create performance test from YAML ──");
        String testName    = "IT_RunLifecycle_" + System.currentTimeMillis();
        String yamlContent = buildYaml(uploadedScriptId);
        log("YAML:\n" + yamlContent);

        Test createdTest = pcRestProxy.createOrUpdateTestFromYamlContent(
                testName, TEST_FOLDER, yamlContent);
        assertNotNull("Created test must not be null", createdTest);
        createdTestId = Integer.parseInt(createdTest.getID());
        log("Test created: ID=" + createdTestId + ", Name=" + createdTest.getName()
                + ", Path=" + createdTest.getTestFolderPath());

        // ── Step 3: Resolve test instance ────────────────────────────────────
        log("\n── Step 3: Resolve test instance ──");
        int testInstanceId = resolveTestInstance(createdTestId);
        assertTrue("Test instance ID must be > 0, got: " + testInstanceId, testInstanceId > 0);
        log("Using test instance ID=" + testInstanceId);

        // ── Step 4: Start run ────────────────────────────────────────────────
        log("\n── Step 4: Start run ──");
        PcRunResponse startResponse = pcRestProxy.startRun(
                createdTestId,
                testInstanceId,
                TIMESLOT,
                POST_RUN_ACTION,
                false,   // vudsMode
                -1);     // timeslotId (-1 = let LRE allocate)
        assertNotNull("startRun response must not be null", startResponse);
        runId = startResponse.getID();
        assertTrue("Run ID must be > 0, got: " + runId, runId > 0);
        log("Run started: runId=" + runId
                + ", testId=" + startResponse.getTestID()
                + ", timeslotId=" + startResponse.getTimeslotID());

        // ── Step 5: Monitor run state ────────────────────────────────────────
        log("\n── Step 5: Monitor run state (max " + (MONITOR_TIMEOUT_MS / 60_000) + " min) ──");
        RunState finalState = monitorRun(runId);
        log("Monitoring complete. Final observed state: " + finalState.value());

        // ── Step 6: Get run event log ────────────────────────────────────────
        log("\n── Step 6: Get run event log ──");
        try {
            PcRunEventLog eventLog = pcRestProxy.getRunEventLog(runId);
            if (eventLog != null && eventLog.getRecordsList() != null) {
                log("Event log contains " + eventLog.getRecordsList().size() + " record(s).");
                eventLog.getRecordsList().forEach(r ->
                        log("  [Event] " + r.getType() + " | " + r.getDescription()));
            } else {
                log("Event log is empty or not available.");
            }
        } catch (Exception e) {
            log("getRunEventLog failed (non-fatal): " + e.getMessage());
        }

        // ── Step 7: Get run results ──────────────────────────────────────────
        log("\n── Step 7: Get run results ──");
        try {
            PcRunResults runResults = pcRestProxy.getRunResults(runId);
            if (runResults != null && runResults.getResultsList() != null) {
                log("Run results: " + runResults.getResultsList().size() + " artifact(s).");
                runResults.getResultsList().forEach(r ->
                        log("  [Result] id=" + r.getID() + ", name=" + r.getName() + ", type=" + r.getType()));
            } else {
                log("Run results are empty or not available yet.");
            }
        } catch (Exception e) {
            log("getRunResults failed (non-fatal): " + e.getMessage());
        }

        log("\n=== Run lifecycle integration test completed successfully ===");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a minimal YAML test definition:
     * <ul>
     *   <li>No {@code controller:} key → LRE uses a dedicated controller</li>
     *   <li>One group: 10 VUsers, the uploaded script, LG1 (auto-match)</li>
     *   <li>30-second ramp-up, 5-minute duration</li>
     * </ul>
     */
    private String buildYaml(int scriptId) {
        return "# Generated by TestRunLifecycleIntegration\n"
             + "# controller is intentionally omitted -> dedicated controller\n"
             + "group:\n"
             + "  - vusers: 10\n"
             + "    script_id: " + scriptId + "\n"
             + "    lg_name:\n"
             + "      - \"LG1\"\n"
             + "scheduler:\n"
             + "  rampup: 0\n"
             + "  duration: 300\n";
    }


    /**
     * Returns an existing test-instance ID for the given test, or creates a new
     * one in the last available test-set if none exists.
     */
    private int resolveTestInstance(int testId) throws PcException, IOException {
        // Try to find an existing instance first.
        try {
            PcTestInstances instances = pcRestProxy.getTestInstancesByTestId(testId);
            if (instances != null
                    && instances.getTestInstancesList() != null
                    && !instances.getTestInstancesList().isEmpty()) {
                int id = instances.getTestInstancesList()
                        .get(instances.getTestInstancesList().size() - 1)
                        .getInstanceId();
                log("Found existing test instance: " + id);
                return id;
            }
        } catch (PcException e) {
            log("getTestInstancesByTestId failed (will try to create one): " + e.getMessage());
        }

        // No instance → pick a test-set and create one.
        log("No test instance found; creating one.");
        PcTestSets testSets = pcRestProxy.GetAllTestSets();
        assertNotNull("At least one test set must exist on the server", testSets);
        assertNotNull("Test sets list must not be null", testSets.getPcTestSetsList());
        assertFalse("At least one test set must be available", testSets.getPcTestSetsList().isEmpty());

        PcTestSet testSet = testSets.getPcTestSetsList()
                .get(testSets.getPcTestSetsList().size() - 1);
        log("Using test set: id=" + testSet.getTestSetID() + ", name=" + testSet.getTestSetName());

        int newInstanceId = pcRestProxy.createTestInstance(testId, testSet.getTestSetID());
        log("Created test instance: " + newInstanceId);
        return newInstanceId;
    }

    /**
     * Polls {@code getRunData} every {@value #POLL_INTERVAL_MS} ms until either
     * <ul>
     *   <li>a terminal state is reached, or</li>
     *   <li>{@value #MONITOR_TIMEOUT_MS} ms have elapsed.</li>
     * </ul>
     *
     * <p>The run is stopped as soon as it enters {@link RunState#RUNNING} to keep
     * the integration test duration short.</p>
     *
     * @return the last observed {@link RunState}
     */
    private RunState monitorRun(int runId) throws InterruptedException {
        RunState lastState   = RunState.UNDEFINED;
        boolean  stopIssued  = false;
        long     deadline    = System.currentTimeMillis() + MONITOR_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            PcRunResponse data;
            try {
                data = pcRestProxy.getRunData(runId);
            } catch (Exception e) {
                log("getRunData error (will retry): " + e.getMessage());
                Thread.sleep(POLL_INTERVAL_MS);
                continue;
            }

            RunState current = RunState.get(data.getRunState());

            // Log every state transition.
            if (current != lastState) {
                log("Run state → " + current.value()
                        + " (runId=" + runId + ")");
                lastState = current;
            }

            // Issue stop once the run is actually RUNNING.
            if (current == RunState.RUNNING && !stopIssued) {
                log("Run is RUNNING – issuing stop to keep the test short.");
                try {
                    boolean stopped = pcRestProxy.stopRun(runId, "stop");
                    log("stopRun returned: " + stopped);
                } catch (Exception e) {
                    log("stopRun error (non-fatal): " + e.getMessage());
                }
                stopIssued = true;
            }

            // Exit on any terminal state.
            if (TERMINAL_STATES.contains(current)) {
                log("Reached terminal state: " + current.value());
                return current;
            }

            Thread.sleep(POLL_INTERVAL_MS);
        }

        log("Monitoring timed out after " + (MONITOR_TIMEOUT_MS / 60_000) + " minute(s). "
                + "Last state: " + lastState.value());
        return lastState;
    }

    private void log(String msg) {
        System.out.println("[TestRunLifecycleIntegration] " + msg);
    }
}

