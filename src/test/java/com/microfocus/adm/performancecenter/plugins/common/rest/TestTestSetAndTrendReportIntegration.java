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
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcTestSet;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcTestSetFolder;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcTestSetFolders;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcTestSets;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestTestSetAndTrendReportIntegration {

    private static final String WEB_PROTOCOL = PcRestProxyBase.WEB_PROTOCOL;
    private static final String LRE_SERVER = PcRestProxyBase.LRE_SERVER_NAME_WITH_TENANT;
    private static final String ALM_DOMAIN = PcRestProxyBase.ALM_DOMAIN;
    private static final String ALM_PROJECT = PcRestProxyBase.ALM_PROJECT;
    private static final String USERNAME = PcRestProxyBase.ALM_USER_NAME;
    private static final String PASSWORD = PcRestProxyBase.ALM_PASSWORD;

    private PcRestProxy pcRestProxy;
    private boolean authenticated;

    @Before
    public void setUp() throws PcException, Exception {
        Assume.assumeTrue("Skipping integration test: integration-tests.properties is missing.",
                IntegrationTestConfig.hasPropertiesFile());
        Assume.assumeTrue("Skipping integration test: missing required LRE connection keys in integration-tests.properties.",
                IntegrationTestConfig.isConfigured());

        pcRestProxy = new PcRestProxy(WEB_PROTOCOL, LRE_SERVER, false, ALM_DOMAIN, ALM_PROJECT, "", "", "");
        authenticated = pcRestProxy.authenticate(USERNAME, PASSWORD);
        assertTrue("Authentication should succeed", authenticated);
    }

    @After
    public void tearDown() {
        if (pcRestProxy != null && authenticated) {
            try {
                pcRestProxy.logout();
            } catch (Exception ignored) {
                // best-effort cleanup
            }
        }
    }

    @Test
    public void createAndListTestSetFolderTestSetAndTrendReport() throws Exception {
        PcTestSetFolders testSetFolders = pcRestProxy.getTestSetFolders();
        assertNotNull(testSetFolders);
        assertNotNull(testSetFolders.getPcTestSetFolderList());
        assertFalse(testSetFolders.getPcTestSetFolderList().isEmpty());

        PcTestSetFolder parentFolder = testSetFolders.getPcTestSetFolderList().get(0);
        String suffix = String.valueOf(System.currentTimeMillis());

        PcTestSetFolder createdFolder = pcRestProxy.createTestSetFolder(parentFolder.getTestSetFolderId(),
                "it_testsetfolder_" + suffix);
        assertNotNull(createdFolder);
        assertTrue(createdFolder.getTestSetFolderId() > 0);
        assertEquals("it_testsetfolder_" + suffix, createdFolder.getTestSetFolderName());

        PcTestSets testSets = pcRestProxy.getAllTestSets();
        assertNotNull(testSets);
        assertNotNull(testSets.getPcTestSetsList());
        assertFalse(testSets.getPcTestSetsList().isEmpty());

        PcTestSet createdTestSet = pcRestProxy.createTestSet("it_testset_" + suffix,
                createdFolder.getTestSetFolderId(), "integration test");
        assertNotNull(createdTestSet);
        assertTrue(createdTestSet.getTestSetID() > 0);
        assertEquals("it_testset_" + suffix, createdTestSet.getTestSetName());
        assertEquals("integration test", createdTestSet.getTestSetComment());
        assertEquals(createdFolder.getTestSetFolderId(), createdTestSet.getTestSetParentId());

        int trendReportId = pcRestProxy.addTrendReport("it_trendreport_" + suffix, "integration test");
        assertTrue(trendReportId > 0);
    }
}
