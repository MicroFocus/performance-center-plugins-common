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
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcScript;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Integration test for the uploadScript method in PcRestProxy.
 *
 * This test verifies the upload of a script to a LoadRunner Enterprise (LRE) server.
 *
 * Requirements:
 * - LRE Server: w22trmssql.aws.swinfra.net
 * - Domain: DANIEL
 * - Project: proj1
 * - Credentials: username=sa, password=saqa
 * - Script to upload: C:\Git\plugin\jenkins-sync\BlackHoleTruClient.zip
 * - LRE Subject path: Subject\daniel
 */
public class TestUploadScriptIntegration {

    private PcRestProxy pcRestProxy;
    private boolean isAuthenticated = false;

    // Test configuration constants (loaded from integration-tests.properties / system properties)
    private static final String WEB_PROTOCOL = PcRestProxyBase.WEB_PROTOCOL;
    private static final String LRE_SERVER = PcRestProxyBase.LRE_SERVER_NAME_WITH_TENANT;
    private static final String ALM_DOMAIN = PcRestProxyBase.ALM_DOMAIN;
    private static final String ALM_PROJECT = PcRestProxyBase.ALM_PROJECT;
    private static final String USERNAME = PcRestProxyBase.ALM_USER_NAME;
    private static final String PASSWORD = PcRestProxyBase.ALM_PASSWORD;
    private static final String SCRIPT_ZIP_PATH = PcRestProxyBase.scriptPath;
    private static final String TEST_FOLDER_PATH = PcRestProxyBase.testFolderPath;

    // Upload parameters
    private static final boolean OVERWRITE = true;
    private static final boolean RUNTIME_ONLY = true;
    private static final boolean KEEP_CHECKED_OUT = false;

    /**
     * Setup method - initializes PcRestProxy and authenticates.
     * This method is called before each test method.
     */
    @Before
    public void setUp() throws PcException, IOException {
        System.out.println("=== setUp: Initializing PcRestProxy ===");
        Assume.assumeTrue("Skipping integration test: integration-tests.properties is missing.",
                IntegrationTestConfig.hasPropertiesFile());
        Assume.assumeTrue("Skipping integration test: missing required LRE connection keys in integration-tests.properties.",
                IntegrationTestConfig.isConfigured());
        Assume.assumeTrue("Skipping upload integration test: missing or placeholder pc.test.scriptPath in integration-tests.properties.",
                IntegrationTestConfig.hasUsableFileValue("pc.test.scriptPath"));
        Assume.assumeTrue("Skipping upload integration test: missing or placeholder pc.test.folderPath in integration-tests.properties.",
                IntegrationTestConfig.hasUsableFileValue("pc.test.folderPath"));

        try {
            // Instantiate PcRestProxy with the specific LRE server
            // Parameters: webProtocol, pcServerName, useToken, almDomain, almProject, proxyOutURL, proxyUser, proxyPassword
            pcRestProxy = new PcRestProxy(
                    WEB_PROTOCOL,
                    LRE_SERVER,
                    false,  // useToken = false for regular auth
                    ALM_DOMAIN,
                    ALM_PROJECT,
                    "",     // proxyOutURL = empty (no proxy)
                    "",     // proxyUser = empty
                    ""      // proxyPassword = empty
            );
            System.out.println("setUp: PcRestProxy instantiated successfully");

            // Authenticate with the server
            System.out.println("setUp: Attempting to authenticate with credentials (username: " + USERNAME + ")");
            isAuthenticated = pcRestProxy.authenticate(USERNAME, PASSWORD);

            if (isAuthenticated) {
                System.out.println("setUp: Authentication successful");
            } else {
                System.out.println("setUp: Authentication returned false (unexpected)");
            }

        } catch (PcException ex) {
            System.out.println("setUp: Failed to initialize or authenticate. PcException = " + ex.getMessage());
            throw ex;
        } catch (IOException ex) {
            System.out.println("setUp: IOException during setup = " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            System.out.println("setUp: Unexpected exception = " + ex.getMessage());
            ex.printStackTrace(System.out);
            throw new PcException("setUp failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Cleanup method - logs out from the server.
     * This method is called after each test method.
     */
    @After
    public void tearDown() {
        System.out.println("\n=== tearDown: Logging out ===");

        if (pcRestProxy != null && isAuthenticated) {
            try {
                boolean logoutSuccess = pcRestProxy.logout();
                if (logoutSuccess) {
                    System.out.println("tearDown: Logout successful");
                } else {
                    System.out.println("tearDown: Logout returned false (unexpected)");
                }
            } catch (Exception ex) {
                System.out.println("tearDown: Logout failed - " + ex.getMessage());
                ex.printStackTrace(System.out);
            }
        }
    }

    /**
     * Integration test for uploadScript method.
     *
     * This test:
     * 1. Verifies the script file exists
     * 2. Uploads the script to the LRE server
     * 3. Verifies the upload was successful (scriptId > 0)
     * 4. Optionally retrieves and displays the uploaded script details
     * 5. Cleans up by deleting the uploaded script
     */
    @Test
    public void testUploadScriptIntegration() throws PcException, IOException {
        System.out.println("\n=== testUploadScriptIntegration: Starting ===");

        try {
            // Step 1: Verify the script file exists
            System.out.println("testUploadScriptIntegration: Verifying script file exists: " + SCRIPT_ZIP_PATH);
            File scriptFile = new File(SCRIPT_ZIP_PATH);
            assertTrue("Script file does not exist at: " + SCRIPT_ZIP_PATH, scriptFile.exists());
            assertTrue("Script file is not readable: " + SCRIPT_ZIP_PATH, scriptFile.canRead());
            System.out.println("testUploadScriptIntegration: Script file found - Size: " + scriptFile.length() + " bytes");

            // Step 2: Upload the script
            System.out.println("testUploadScriptIntegration: Uploading script");
            System.out.println("  - Test Folder Path: " + TEST_FOLDER_PATH);
            System.out.println("  - Overwrite: " + OVERWRITE);
            System.out.println("  - Runtime Only: " + RUNTIME_ONLY);
            System.out.println("  - Keep Checked Out: " + KEEP_CHECKED_OUT);
            System.out.println("  - Script Path: " + SCRIPT_ZIP_PATH);

            int scriptId = pcRestProxy.uploadScript(
                    TEST_FOLDER_PATH,
                    OVERWRITE,
                    RUNTIME_ONLY,
                    KEEP_CHECKED_OUT,
                    SCRIPT_ZIP_PATH
            );

            // Step 3: Verify upload was successful
            System.out.println("testUploadScriptIntegration: Upload completed - Script ID: " + scriptId);
            assertTrue("Script ID should be greater than 0, but got: " + scriptId, scriptId > 0);

            // Step 4: Retrieve and display the uploaded script details
            System.out.println("testUploadScriptIntegration: Retrieving uploaded script details");
            PcScript uploadedScript = pcRestProxy.getScript(scriptId);

            assertNotNull("Retrieved script should not be null", uploadedScript);
            System.out.println("testUploadScriptIntegration: Script Details:");
            System.out.println("  - ID: " + uploadedScript.getID());
            System.out.println("  - Name: " + uploadedScript.getName());
            System.out.println("  - Test Folder Path: " + uploadedScript.getTestFolderPath());
            System.out.println("  - Created By: " + uploadedScript.getCreatedBy());
            System.out.println("  - Working Mode: " + uploadedScript.getWorkingMode());
            System.out.println("  - Protocol: " + uploadedScript.getProtocol());

            // Verify script properties
            assertEquals("Script ID should match", scriptId, uploadedScript.getID());
            assertNotNull("Script name should not be null", uploadedScript.getName());
            assertTrue("Test folder path should contain 'daniel'",
                    uploadedScript.getTestFolderPath().toLowerCase().contains("daniel"));

            // Step 5: Clean up - delete the uploaded script
            System.out.println("testUploadScriptIntegration: Cleaning up - deleting uploaded script");
            boolean deleteSuccess = pcRestProxy.deleteScript(scriptId);
            assertTrue("Script deletion should succeed", deleteSuccess);
            System.out.println("testUploadScriptIntegration: Script deleted successfully");

        } catch (PcException ex) {
            System.out.println("testUploadScriptIntegration: Failed with PcException = " + ex.getMessage());
            throw ex;
        } catch (IOException ex) {
            System.out.println("testUploadScriptIntegration: Failed with IOException = " + ex.getMessage());
            throw ex;
        } catch (AssertionError ex) {
            System.out.println("testUploadScriptIntegration: Assertion failed = " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            System.out.println("testUploadScriptIntegration: Unexpected exception = " + ex.getMessage());
            ex.printStackTrace(System.out);
            throw new PcException("testUploadScriptIntegration failed: " + ex.getMessage(), ex);
        } finally {
            System.out.println("=== testUploadScriptIntegration: Ending ===\n");
        }
    }

    /**
     * Test to verify authentication is working correctly.
     * This is a prerequisite test to ensure the setup is correct.
     */
    @Test
    public void testAuthentication() throws PcException, IOException {
        System.out.println("\n=== testAuthentication: Starting ===");

        try {
            System.out.println("testAuthentication: Verifying authentication was successful");
            assertTrue("Authentication should have been successful", isAuthenticated);
            System.out.println("testAuthentication: Authentication verified");

        } catch (AssertionError ex) {
            System.out.println("testAuthentication: Assertion failed = " + ex.getMessage());
            throw ex;
        } finally {
            System.out.println("=== testAuthentication: Ending ===\n");
        }
    }

    /**
     * Test to verify connectivity to the LRE server.
     * This test attempts to retrieve test plan folders from the server.
     */
    @Test
    public void testServerConnectivity() throws PcException, IOException {
        System.out.println("\n=== testServerConnectivity: Starting ===");

        try {
            System.out.println("testServerConnectivity: Attempting to retrieve test plan folders");
            assertNotNull("PcRestProxy should not be null", pcRestProxy);

            // This call will fail if we can't connect to the server
            pcRestProxy.getTestPlanFolders();
            System.out.println("testServerConnectivity: Successfully retrieved test plan folders");

        } catch (PcException ex) {
            System.out.println("testServerConnectivity: Failed to connect - PcException = " + ex.getMessage());
            throw ex;
        } catch (IOException ex) {
            System.out.println("testServerConnectivity: Failed to connect - IOException = " + ex.getMessage());
            throw ex;
        } finally {
            System.out.println("=== testServerConnectivity: Ending ===\n");
        }
    }
}

