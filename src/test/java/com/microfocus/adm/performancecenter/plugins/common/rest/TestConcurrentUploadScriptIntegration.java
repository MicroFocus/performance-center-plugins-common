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
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Integration tests that exercise {@link PcRestProxy} concurrently against a live LoadRunner
 * Enterprise (LRE) server, validating the per-request {@code HttpContext} thread-safety fix.
 *
 * <h2>What these tests prove</h2>
 * <ul>
 *     <li>{@link #testSingleInstanceIsThreadSafeForConcurrentReads()} — a single shared
 *         {@code PcRestProxy} instance can serve many simultaneous requests from different threads
 *         (each request gets its own context while reusing the shared, authenticated session).</li>
 *     <li>{@link #testConcurrentUploadsWithIsolatedSessions()} — several scripts can be uploaded in
 *         parallel by giving each worker thread its own authenticated {@code PcRestProxy}.</li>
 * </ul>
 *
 * <h2>Important server-side constraint (verified against a live LRE server)</h2>
 * The LRE REST API <strong>serializes script uploads per authenticated session</strong>: two
 * multipart {@code POST /Scripts} requests issued simultaneously on the <em>same</em> session result
 * in one {@code 201 Created} and one {@code 400 Bad Request} ("…a multipart/form-data type request
 * should be sent…", error code 1600), even though both requests are well-formed multipart bodies.
 * Therefore, to upload several scripts "at once", give each worker thread its own session (one
 * {@code PcRestProxy} + one {@code authenticate(...)} per thread), as demonstrated below. Concurrent
 * read operations on a single shared instance are fully supported.
 *
 * <p>These tests are skipped automatically when {@code integration-tests.properties} is missing or
 * not configured with a reachable LRE server and a usable script file.
 */
public class TestConcurrentUploadScriptIntegration {

    /** Number of scripts uploaded in parallel. Adjust to increase/decrease contention. */
    private static final int CONCURRENT_UPLOADS =
            Integer.parseInt(IntegrationTestConfig.get("pc.test.concurrentUploads", "5"));

    /** Number of threads used for the concurrent-read thread-safety check. */
    private static final int CONCURRENT_READERS = 8;

    private static final String WEB_PROTOCOL = PcRestProxyBase.WEB_PROTOCOL;
    private static final String LRE_SERVER = PcRestProxyBase.LRE_SERVER_NAME_WITH_TENANT;
    private static final String ALM_DOMAIN = PcRestProxyBase.ALM_DOMAIN;
    private static final String ALM_PROJECT = PcRestProxyBase.ALM_PROJECT;
    private static final String USERNAME = PcRestProxyBase.ALM_USER_NAME;
    private static final String PASSWORD = PcRestProxyBase.ALM_PASSWORD;
    private static final String SCRIPT_ZIP_PATH = PcRestProxyBase.scriptPath;
    private static final String BASE_FOLDER_PATH = PcRestProxyBase.testFolderPath;

    private static final boolean OVERWRITE = true;
    private static final boolean RUNTIME_ONLY = true;
    private static final boolean KEEP_CHECKED_OUT = false;

    private PcRestProxy sharedProxy;
    private boolean isAuthenticated = false;

    @Before
    public void setUp() throws PcException, IOException {
        System.out.println("=== setUp: Initializing shared PcRestProxy ===");
        Assume.assumeTrue("Skipping integration test: integration-tests.properties is missing.",
                IntegrationTestConfig.hasPropertiesFile());
        Assume.assumeTrue("Skipping integration test: missing required LRE connection keys in integration-tests.properties.",
                IntegrationTestConfig.isConfigured());
        Assume.assumeTrue("Skipping upload integration test: missing or placeholder pc.test.scriptPath in integration-tests.properties.",
                IntegrationTestConfig.hasUsableFileValue("pc.test.scriptPath"));
        Assume.assumeTrue("Skipping upload integration test: missing or placeholder pc.test.folderPath in integration-tests.properties.",
                IntegrationTestConfig.hasUsableFileValue("pc.test.folderPath"));

        sharedProxy = newAuthenticatedProxy();
        isAuthenticated = true;
        System.out.println("setUp: Shared PcRestProxy authenticated successfully");
    }

    @After
    public void tearDown() {
        System.out.println("\n=== tearDown: Logging out shared proxy ===");
        if (sharedProxy != null && isAuthenticated) {
            try {
                sharedProxy.logout();
                System.out.println("tearDown: Logout successful");
            } catch (Exception ex) {
                System.out.println("tearDown: Logout failed - " + ex.getMessage());
            }
        }
    }

    /**
     * Drives many concurrent read requests through a single shared proxy instance. Before the
     * thread-safety fix (a single shared mutable {@code HttpContext}), concurrent requests raced on
     * the context; with the fix each request uses its own context and all calls succeed.
     */
    @Test
    public void testSingleInstanceIsThreadSafeForConcurrentReads() throws Exception {
        System.out.println("\n=== testSingleInstanceIsThreadSafeForConcurrentReads: Starting ("
                + CONCURRENT_READERS + " readers) ===");

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_READERS);
        CountDownLatch ready = new CountDownLatch(CONCURRENT_READERS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successfulCalls = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<Future<?>>();

        try {
            for (int i = 0; i < CONCURRENT_READERS; i++) {
                futures.add(pool.submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        ready.countDown();
                        start.await();
                        for (int r = 0; r < 5; r++) {
                            assertNotNull("getScripts() must not return null", sharedProxy.getScripts());
                            assertNotNull("getTestPlanFolders() must not return null",
                                    sharedProxy.getTestPlanFolders());
                            successfulCalls.addAndGet(2);
                        }
                        return null;
                    }
                }));
            }

            assertTrue("Reader threads did not become ready in time", ready.await(30, TimeUnit.SECONDS));
            start.countDown(); // unleash all readers simultaneously

            for (Future<?> future : futures) {
                future.get(120, TimeUnit.SECONDS); // propagates any thread's exception as a failure
            }

            assertEquals("Every concurrent read call should have completed successfully",
                    CONCURRENT_READERS * 5 * 2, successfulCalls.get());
            System.out.println("testSingleInstanceIsThreadSafeForConcurrentReads: "
                    + successfulCalls.get() + " concurrent read calls all succeeded");
        } finally {
            pool.shutdownNow();
            System.out.println("=== testSingleInstanceIsThreadSafeForConcurrentReads: Ending ===\n");
        }
    }

    /**
     * Uploads several scripts in parallel, one authenticated session per worker thread. This honors
     * the LRE server's per-session upload serialization (see class Javadoc) and demonstrates the
     * recommended pattern for "uploading multiple scripts at once".
     */
    @Test
    public void testConcurrentUploadsWithIsolatedSessions() throws Exception {
        System.out.println("\n=== testConcurrentUploadsWithIsolatedSessions: Starting ("
                + CONCURRENT_UPLOADS + " uploads) ===");

        File scriptFile = new File(SCRIPT_ZIP_PATH);
        assertTrue("Script file does not exist at: " + SCRIPT_ZIP_PATH, scriptFile.exists());
        assertTrue("Script file is not readable: " + SCRIPT_ZIP_PATH, scriptFile.canRead());

        // Pre-create distinct target sub-folders sequentially (one per upload) so each upload yields
        // a distinct (folder, script-name) pair and the concurrent phase only performs uploads.
        List<String> targetFolders = new ArrayList<String>();
        String[] folders = new String[CONCURRENT_UPLOADS];
        for (int i = 0; i < CONCURRENT_UPLOADS; i++) {
            String folder = BASE_FOLDER_PATH + "\\concurrent" + i;
            targetFolders.add(folder);
            folders[i] = folder;
        }
        System.out.println("Ensuring target folders exist: " + targetFolders);
        sharedProxy.createTestPlanFolders(folders);

        ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_UPLOADS);
        CountDownLatch ready = new CountDownLatch(CONCURRENT_UPLOADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
        List<Integer> uploadedScriptIds = Collections.synchronizedList(new ArrayList<Integer>());

        try {
            for (int i = 0; i < CONCURRENT_UPLOADS; i++) {
                final String folder = targetFolders.get(i);
                futures.add(pool.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        // Each worker uses its own authenticated session (the server serializes
                        // uploads within a single session).
                        PcRestProxy workerProxy = newAuthenticatedProxy();
                        try {
                            ready.countDown();
                            start.await();
                            int scriptId = workerProxy.uploadScript(
                                    folder, OVERWRITE, RUNTIME_ONLY, KEEP_CHECKED_OUT, SCRIPT_ZIP_PATH);
                            System.out.println("Uploaded script id " + scriptId + " into " + folder);
                            return scriptId;
                        } finally {
                            try {
                                workerProxy.logout();
                            } catch (Exception ignored) {
                                // best-effort logout
                            }
                        }
                    }
                }));
            }

            assertTrue("Worker threads did not become ready in time", ready.await(30, TimeUnit.SECONDS));
            start.countDown(); // release all uploads at once

            Set<Integer> distinctIds = new HashSet<Integer>();
            for (Future<Integer> future : futures) {
                int scriptId = future.get(180, TimeUnit.SECONDS);
                assertTrue("Script ID should be greater than 0, but got: " + scriptId, scriptId > 0);
                uploadedScriptIds.add(scriptId);
                distinctIds.add(scriptId);
            }

            assertEquals("Every concurrent upload should produce a result",
                    CONCURRENT_UPLOADS, uploadedScriptIds.size());
            assertEquals("Each upload (distinct folder) should yield a distinct script ID",
                    CONCURRENT_UPLOADS, distinctIds.size());

            // Verify each uploaded script is independently retrievable via the shared proxy.
            for (int scriptId : uploadedScriptIds) {
                assertNotNull("Uploaded script should be retrievable: " + scriptId,
                        sharedProxy.getScript(scriptId));
            }
            System.out.println("testConcurrentUploadsWithIsolatedSessions: All uploads succeeded with "
                    + "distinct IDs: " + uploadedScriptIds);
        } finally {
            pool.shutdownNow();
            // Best-effort cleanup of uploaded scripts via the shared proxy.
            for (int scriptId : uploadedScriptIds) {
                try {
                    sharedProxy.deleteScript(scriptId);
                    System.out.println("Cleaned up script id " + scriptId);
                } catch (Exception ex) {
                    System.out.println("Cleanup failed for script id " + scriptId + " - " + ex.getMessage());
                }
            }
            System.out.println("=== testConcurrentUploadsWithIsolatedSessions: Ending ===\n");
        }
    }

    private PcRestProxy newAuthenticatedProxy() throws PcException, IOException {
        PcRestProxy proxy = new PcRestProxy(
                WEB_PROTOCOL,
                LRE_SERVER,
                false,
                ALM_DOMAIN,
                ALM_PROJECT,
                "",
                "",
                "");
        boolean authenticated = proxy.authenticate(USERNAME, PASSWORD);
        assertTrue("Authentication should have been successful", authenticated);
        return proxy;
    }
}
