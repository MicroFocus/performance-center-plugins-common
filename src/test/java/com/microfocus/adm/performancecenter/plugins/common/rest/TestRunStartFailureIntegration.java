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

import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcException;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcRunResponse;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcTestInstance;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcTestInstances;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcTestSet;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcTestSets;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PostRunAction;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.TimeslotDuration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration test that validates startRun fails with a meaningful message
 * when YAML requests more automatch LGs than available.
 */
public class TestRunStartFailureIntegration {

    private static final String WEB_PROTOCOL = PcRestProxyBase.WEB_PROTOCOL;
    private static final String LRE_SERVER = PcRestProxyBase.LRE_SERVER_NAME_WITH_TENANT;
    private static final String ALM_DOMAIN = PcRestProxyBase.ALM_DOMAIN;
    private static final String ALM_PROJECT = PcRestProxyBase.ALM_PROJECT;
    private static final String USERNAME = PcRestProxyBase.ALM_USER_NAME;
    private static final String PASSWORD = PcRestProxyBase.ALM_PASSWORD;
    private static final String KILIMANJARO_ZIP = PcRestProxyBase.kilimanjaroScriptPath;
    private static final String SCRIPT_FOLDER = PcRestProxyBase.kilimanjaroFolderPath;

    private static final String TEST_FOLDER = "Subject\\scripts";
    private static final TimeslotDuration TIMESLOT = new TimeslotDuration(0, 30);
    private static final String POST_RUN_ACTION = PostRunAction.COLLATE_AND_ANALYZE.getValue();

    private static final String[] OVERCOMMITTED_LGS = {
            "LG1", "LG2", "LG3", "LG4", "LG5",
            "LG6", "LG7", "LG8", "LG9", "LG10"
    };

    private PcRestProxy pcRestProxy;
    private boolean isAuthenticated = false;
    private int uploadedScriptId = 0;
    private int createdTestId = 0;
    private int runId = 0;

    @Before
    public void setUp() throws PcException, IOException {
        log("=== setUp ===");
        Assume.assumeTrue("Skipping integration test: integration-tests.properties is missing.",
                IntegrationTestConfig.hasPropertiesFile());
        Assume.assumeTrue("Skipping integration test: missing required LRE connection keys in integration-tests.properties.",
                IntegrationTestConfig.isConfigured());
        Assume.assumeTrue("Skipping run-start failure integration test: missing or placeholder pc.test.kilimanjaro.scriptPath in integration-tests.properties.",
                IntegrationTestConfig.hasUsableFileValue("pc.test.kilimanjaro.scriptPath"));
        Assume.assumeTrue("Skipping run-start failure integration test: missing or placeholder pc.test.kilimanjaro.folderPath in integration-tests.properties.",
                IntegrationTestConfig.hasUsableFileValue("pc.test.kilimanjaro.folderPath"));

        pcRestProxy = new PcRestProxy(
                WEB_PROTOCOL, LRE_SERVER,
                false,
                ALM_DOMAIN, ALM_PROJECT,
                "", "", "");

        isAuthenticated = pcRestProxy.authenticate(USERNAME, PASSWORD);
        assertTrue("Authentication must succeed", isAuthenticated);
    }

    @After
    public void tearDown() {
        log("=== tearDown ===");

        if (runId > 0) {
            try {
                pcRestProxy.stopRun(runId, "stop");
            } catch (Exception ignored) {
                log("Stop run ignored: " + ignored.getMessage());
            }
        }

        if (createdTestId > 0) {
            try {
                boolean deleted = pcRestProxy.deleteTest(createdTestId);
                log("Deleted test ID=" + createdTestId + ": " + deleted);
            } catch (Exception ignored) {
                log("Delete test ignored: " + ignored.getMessage());
            }
        }

        if (uploadedScriptId > 0) {
            try {
                boolean deleted = pcRestProxy.deleteScript(uploadedScriptId);
                log("Deleted script ID=" + uploadedScriptId + ": " + deleted);
            } catch (Exception ignored) {
                log("Delete script ignored: " + ignored.getMessage());
            }
        }

        if (pcRestProxy != null && isAuthenticated) {
            try {
                pcRestProxy.logout();
                log("Logged out.");
            } catch (Exception ignored) {
                log("Logout ignored: " + ignored.getMessage());
            }
        }
    }

    @org.junit.Test
    public void testStartRunFailsWhenInsufficientAutomatchLGs() throws Exception {

        log("\n-- Step 1: Upload kilimanjaro.zip to " + SCRIPT_FOLDER + " --");
        File scriptFile = new File(KILIMANJARO_ZIP);
        assertTrue("kilimanjaro.zip must exist at: " + scriptFile.getAbsolutePath(), scriptFile.exists());

        uploadedScriptId = pcRestProxy.uploadScript(
                SCRIPT_FOLDER,
                true,
                true,
                false,
                KILIMANJARO_ZIP);
        assertTrue("Uploaded script ID must be > 0, got: " + uploadedScriptId, uploadedScriptId > 0);

        log("\n-- Step 2: Create overcommitted YAML test (LG1..LG10) --");
        String testName = "IT_StartRunFails_LGs_" + System.currentTimeMillis();
        Test createdTest = pcRestProxy.createOrUpdateTestFromYamlContent(
                testName,
                TEST_FOLDER,
                buildYamlWithAutomatchLGs(uploadedScriptId, OVERCOMMITTED_LGS));
        assertNotNull("Created test must not be null", createdTest);
        createdTestId = Integer.parseInt(createdTest.getID());

        log("\n-- Step 3: Resolve test instance --");
        int testInstanceId = resolveTestInstance(createdTestId);
        assertTrue("Test instance ID must be > 0, got: " + testInstanceId, testInstanceId > 0);

        log("\n-- Step 4: startRun should fail due to unavailable LG resources --");
        try {
            PcRunResponse startResponse = pcRestProxy.startRun(
                    createdTestId,
                    testInstanceId,
                    TIMESLOT,
                    POST_RUN_ACTION,
                    false,
                    -1);

            if (startResponse != null && startResponse.getID() > 0) {
                runId = startResponse.getID();
                try {
                    pcRestProxy.stopRun(runId, "stop");
                } catch (Exception ignored) {
                    log("Best-effort stop ignored: " + ignored.getMessage());
                }
            }
            fail("Expected startRun to fail because LG1..LG10 are not all available, but run started.");
        } catch (PcException expected) {
            String errorMessage = expected.getMessage() == null ? "" : expected.getMessage();
            String normalized = errorMessage.toLowerCase(Locale.ROOT);

            assertFalse("Failure message should not be empty", normalized.isEmpty());
            assertTrue(
                    "Expected startRun failure message to mention LG/resource/timeslot constraints, but was: " + errorMessage,
                    normalized.contains("lg")
                            || normalized.contains("resource")
                            || normalized.contains("timeslot")
                            || normalized.contains("host")
                            || normalized.contains("available")
            );
            log("startRun failed as expected: " + errorMessage);
        }
    }

    private int resolveTestInstance(int testId) throws PcException, IOException {
        try {
            PcTestInstances instances = pcRestProxy.getTestInstancesByTestId(testId);
            if (instances != null
                    && instances.getTestInstancesList() != null
                    && !instances.getTestInstancesList().isEmpty()) {
                PcTestInstance existing = instances.getTestInstancesList()
                        .get(instances.getTestInstancesList().size() - 1);
                return existing.getInstanceId();
            }
        } catch (PcException ignored) {
            log("getTestInstancesByTestId failed; creating a new one.");
        }

        PcTestSets testSets = pcRestProxy.GetAllTestSets();
        assertNotNull("At least one test set must exist on the server", testSets);
        assertNotNull("Test sets list must not be null", testSets.getPcTestSetsList());
        assertFalse("At least one test set must be available", testSets.getPcTestSetsList().isEmpty());

        PcTestSet testSet = testSets.getPcTestSetsList().get(testSets.getPcTestSetsList().size() - 1);
        return pcRestProxy.createTestInstance(testId, testSet.getTestSetID());
    }

    private String buildYamlWithAutomatchLGs(int scriptId, String[] lgNames) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("# Generated by TestRunStartFailureIntegration\n")
                .append("# intentionally requests many LGs to trigger startRun allocation failure\n")
                .append("group:\n")
                .append("  - vusers: 10\n")
                .append("    script_id: ").append(scriptId).append("\n")
                .append("    lg_name:\n");

        for (String lgName : lgNames) {
            yaml.append("      - \"").append(lgName).append("\"\n");
        }

        yaml.append("scheduler:\n")
                .append("  rampup: 0\n")
                .append("  duration: 300\n");
        return yaml.toString();
    }

    private void log(String message) {
        System.out.println("[TestRunStartFailureIntegration] " + message);
    }
}

