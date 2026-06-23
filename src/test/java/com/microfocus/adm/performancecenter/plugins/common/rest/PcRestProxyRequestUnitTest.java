package com.microfocus.adm.performancecenter.plugins.common.rest;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.*;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.Content;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PcRestProxyRequestUnitTest {

    @Test
    public void authenticate_andCreateOrUpdateOverloads_areCoveredByUnitTests() throws Exception {
        StubPcRestProxy authProxy = new StubPcRestProxy(false);
        authProxy.registerResponse("GET", "/authentication-point/authenticate", "<ok/>");
        Assert.assertTrue(authProxy.authenticate("sa", "saqa"));

        StubPcRestProxy tokenProxy = new StubPcRestProxy(true);
        tokenProxy.registerResponse("POST", "/authentication-point/authenticateclient", "<ok/>");
        Assert.assertTrue(tokenProxy.authenticate("id-key", "secret-key"));

        StubPcRestProxy createProxy = new StubPcRestProxy(false);
        createProxy.registerResponse("GET", "/testplan", "<TestPlanFolders><TestPlanFolder><Id>1</Id><ParentId>0</ParentId><Name>folder</Name><FullPath>Subject\\folder</FullPath></TestPlanFolder></TestPlanFolders>");
        createProxy.registerResponse("POST", "/tests", "<Test><ID>11</ID><Name>myFromYaml</Name><TestFolderPath>Subject\\folder</TestFolderPath></Test>");
        createProxy.registerResponse("GET", "/Scripts/1", "<Script><ID>1</ID><Name>s1</Name><TestFolderPath>Subject\\folder</TestFolderPath><Protocol>WEB</Protocol></Script>");

        String yamlTest =
                "test_name: myFromYaml\n" +
                "test_folder_path: folder\n" +
                "test_content:\n" +
                "  scheduler:\n" +
                "    rampup: 1\n" +
                "    duration: 10\n" +
                "  group:\n" +
                "    - script_id: 1\n" +
                "      vusers: 1\n";

        Assert.assertEquals("11", createProxy.createOrUpdateTestFromYamlTest(yamlTest).getID());
        Assert.assertEquals("11", createProxy.createOrUpdateTestFromYamlContent("", "", yamlTest).getID());

        String contentXml = new Content().objectToXML(true);
        Assert.assertEquals("11", createProxy.createOrUpdateTest("xmlPath", "folder", contentXml).getID());
    }

    @Test
    public void requestMethods_returnParsedObjectsAndFlags() throws Exception {
        StubPcRestProxy proxy = new StubPcRestProxy();
        proxy.registerResponse("POST", "/Runs", "<Run><ID>13</ID><TestID>1</TestID><TestInstanceID>2</TestInstanceID><Duration>10</Duration><RunState>Running</RunState></Run>");
        proxy.registerResponse("POST", "/testinstances", "<TestInstance><TestInstanceID>55</TestInstanceID></TestInstance>");
        proxy.registerResponse("GET", "/testsets", "<TestSets><TestSet><ID>1</ID><Name>set1</Name></TestSet></TestSets>");
        proxy.registerResponse("GET", "/testsetfolders", "<TestSetFolders><TestSetFolder><TestSetFolderId>10</TestSetFolderId><Parent>0</Parent><TestSetFolderName>Root</TestSetFolderName></TestSetFolder></TestSetFolders>");
        proxy.registerResponse("POST", "/testsetfolders", "<TestSetFolder><TestSetFolderId>11</TestSetFolderId><Parent>10</Parent><TestSetFolderName>NewFolder</TestSetFolderName></TestSetFolder>");
        proxy.registerResponse("POST", "/testsets", "<TestSet><TestSetName>NewSet</TestSetName><TestSetComment>demo</TestSetComment><TestSetParentId>11</TestSetParentId><TestSetID>21</TestSetID></TestSet>");
        proxy.registerResponse("GET", "/testinstances?", "<TestInstances><TestInstance><ID>12</ID></TestInstance></TestInstances>");
        proxy.registerResponse("POST", "/Runs/13/stop", "<ok/>");
        proxy.registerResponse("GET", "/Runs/13", "<Run><ID>13</ID><TestID>1</TestID><TestInstanceID>2</TestInstanceID><Duration>10</Duration><RunState>Running</RunState></Run>");
        proxy.registerResponse("GET", "/tests/7", "<Test><ID>7</ID><Name>demo</Name><AutomaticTrending><ReportId>99</ReportId></AutomaticTrending></Test>");
        proxy.registerResponse("GET", "/Runs/13/Results", "<RunResults><RunResult><ID>1</ID><Name>r1</Name><Type>Output Log</Type><RunID>13</RunID></RunResult></RunResults>");
        proxy.registerResponse("GET", "/TrendReports/9/13", "<Root></Root>");
        proxy.registerResponse("POST", "/TrendReports/9", "<ok/>");
        proxy.registerResponse("POST", "/TrendReports", "<TrendReport><ID>41</ID></TrendReport>");
        proxy.registerResponse("GET", "/TrendReports/9", "<TrendReport><TrendedRun><RunID>13</RunID><State>Finished</State></TrendedRun></TrendReport>");
        proxy.registerResponse("GET", "/Runs/13/EventLog", "<EventLog><Record><Message>m</Message></Record></EventLog>");
        proxy.registerResponse("GET", "/timeslots?", "<Timeslots><Timeslot><ID>1</ID><OpenStatus>open</OpenStatus></Timeslot><Timeslot><ID>2</ID><OpenStatus>closed</OpenStatus></Timeslot></Timeslots>");
        proxy.registerResponse("GET", "/authentication-point/logout", "<ok/>");

        PcRunResponse runResponse = proxy.startRun(1, 2, new TimeslotDuration(0, 10), "Collate", false, 3);
        Assert.assertEquals(13, runResponse.getID());
        Assert.assertEquals(55, proxy.createTestInstance(1, 2));
        Assert.assertEquals(1, proxy.GetAllTestSets().getPcTestSetsList().size());
        Assert.assertEquals(1, proxy.getAllTestSets().getPcTestSetsList().size());
        Assert.assertEquals(1, proxy.getTestSetFolders().getPcTestSetFolderList().size());
        Assert.assertEquals(11, proxy.createTestSetFolder(10, "NewFolder").getTestSetFolderId());
        Assert.assertEquals(21, proxy.createTestSet("NewSet", 11, "demo").getTestSetID());
        Assert.assertEquals(41, proxy.addTrendReport("trend-1", "demo"));
        Assert.assertEquals(1, proxy.getTestInstancesByTestId(7).getTestInstancesList().size());
        Assert.assertTrue(proxy.stopRun(13, "stop"));
        Assert.assertEquals(13, proxy.getRunData(13).getID());
        Assert.assertEquals(7, proxy.getTestData(7).getTestId());
        Assert.assertEquals(1, proxy.getRunResults(13).getResultsList().size());
        Assert.assertNotNull(proxy.getTrendReportByXML("9", 13));
        Assert.assertTrue(proxy.updateTrendReport("9", new TrendReportRequest("proj1", 13, new TrendedRange())));
        Assert.assertEquals(1, proxy.getTrendReportMetaData("9").size());
        Assert.assertEquals(1, proxy.getRunEventLog(13).getRecordsList().size());
        Assert.assertEquals(1, proxy.GetOpenTimeslotsByTestId(77).getTimeslotsList().size());
        Assert.assertTrue(proxy.logout());
    }

    @Test
    public void rawRequestMethods_handleStreams() throws Exception {
        StubPcRestProxy proxy = new StubPcRestProxy();
        proxy.registerRawResponse("GET", "/Runs/13/Results/1/data", "result-bytes".getBytes(StandardCharsets.UTF_8));
        proxy.registerRawResponse("GET", "/TrendReports/9/data", "pdf-content".getBytes(StandardCharsets.UTF_8));

        File outputFile = File.createTempFile("pc-run-result", ".bin");
        outputFile.deleteOnExit();
        Assert.assertTrue(proxy.GetRunResultData(13, 1, outputFile.getAbsolutePath()));
        Assert.assertEquals("result-bytes", new String(Files.readAllBytes(outputFile.toPath()), StandardCharsets.UTF_8));

        try (InputStream in = proxy.getTrendingPDF("9")) {
            Assert.assertEquals("pdf-content", new String(readAllBytes(in), StandardCharsets.UTF_8));
        }
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    @Test
    public void scriptAndFolderMethods_areCovered() throws Exception {
        StubPcRestProxy proxy = new StubPcRestProxy();
        proxy.registerResponse("GET", "/Scripts", "<Scripts><Script><ID>8</ID><Name>ScriptA</Name><TestFolderPath>Subject\\folder</TestFolderPath></Script></Scripts>");
        proxy.registerResponse("GET", "/Scripts/8", "<Script><ID>8</ID><Name>ScriptA</Name><TestFolderPath>Subject\\folder</TestFolderPath></Script>");
        proxy.registerResponse("DELETE", "/Scripts/8", "<ok/>");
        proxy.registerResponse("GET", "/testplan", "<TestPlanFolders><TestPlanFolder><Id>1</Id><ParentId>0</ParentId><Name>folder</Name><FullPath>Subject\\folder</FullPath></TestPlanFolder></TestPlanFolders>");
        proxy.registerResponse("POST", "/testplan", "<TestPlanFolder><Id>22</Id><ParentId>1</ParentId><Name>newFolder</Name><FullPath>Subject\\newFolder</FullPath></TestPlanFolder>");
        proxy.registerResponse("POST", "/Scripts", "<Script><ID>77</ID></Script>");

        Assert.assertEquals(1, proxy.getScripts().getPcScriptList().size());
        Assert.assertEquals(8, proxy.getScript(8).getID());
        Assert.assertEquals("ScriptA", proxy.getScript("Subject\\folder", "ScriptA").getName());
        Assert.assertTrue(proxy.deleteScript(8));

        Assert.assertEquals(1, proxy.getTestPlanFolders().getPcTestPlanFolderList().size());
        Assert.assertTrue(proxy.verifyTestPlanFolderExist("Subject\\folder"));
        Assert.assertEquals(22, proxy.createTestPlanFolder("Subject", "newFolder").getId());
        Assert.assertEquals(1, proxy.createTestPlanFolders(new String[]{"Subject\\folder\\child"}).size());

        ArrayList<String[]> paths = new ArrayList<String[]>();
        paths.add(new String[]{"Subject", "newFolder"});
        Assert.assertEquals(1, proxy.createPcTestPlanFolders(paths).size());

        Assert.assertEquals(77, proxy.uploadScript("Subject\\folder", true, true, false,
                "C:\\Git\\plugin\\performance-center-plugins-common\\src\\test\\resources\\microfocus\\adm\\performancecenter\\plugins\\common\\rest\\CreateTest.xml"));
    }

    @Test
    public void uploadKilimanjaroScript_andCreateTestFromYaml_withDedicatedControllerAndLG1() throws Exception {
        String kilimanjaro = new File(getClass().getResource(
                "/microfocus/adm/performancecenter/plugins/common/rest/kilimanjaro.zip"
        ).toURI()).getAbsolutePath();

        StubPcRestProxy proxy = new StubPcRestProxy();

        // Return both target folders so no folder-creation attempt is made
        proxy.registerResponse("GET", "/testplan",
                "<TestPlanFolders>" +
                "<TestPlanFolder><Id>1</Id><ParentId>0</ParentId><Name>scripts</Name><FullPath>Subject\\scripts</FullPath></TestPlanFolder>" +
                "<TestPlanFolder><Id>2</Id><ParentId>0</ParentId><Name>tests</Name><FullPath>Subject\\tests</FullPath></TestPlanFolder>" +
                "<TestPlanFolder><Id>3</Id><ParentId>2</ParentId><Name>kilimanjaro</Name><FullPath>Subject\\tests\\kilimanjaro</FullPath></TestPlanFolder>" +
                "</TestPlanFolders>");

        // Upload kilimanjaro.zip to Subject\scripts
        proxy.registerResponse("POST", "/Scripts",
                "<Script><ID>101</ID><Name>kilimanjaro</Name><TestFolderPath>Subject\\scripts</TestFolderPath></Script>");

        int scriptId = proxy.uploadScript("Subject\\scripts", true, true, false, kilimanjaro);
        Assert.assertEquals(101, scriptId);

        // Script lookup when the YAML resolver resolves script_id -> script details
        proxy.registerResponse("GET", "/Scripts/101",
                "<Script><ID>101</ID><Name>kilimanjaro</Name><TestFolderPath>Subject\\scripts</TestFolderPath><Protocol>WEB</Protocol></Script>");

        // Test creation response
        proxy.registerResponse("POST", "/tests",
                "<Test><ID>42</ID><Name>kilimanjaro_test</Name><TestFolderPath>Subject\\tests\\kilimanjaro</TestFolderPath></Test>");

        // No controller => dedicated (automatch); LG1 => automatch load generator
        String yaml =
                "test_name: kilimanjaro_test\n" +
                "test_folder_path: tests\\kilimanjaro\n" +
                "test_content:\n" +
                "  group:\n" +
                "    - group_name: kilimanjaro_group\n" +
                "      vusers: 10\n" +
                "      script_id: " + scriptId + "\n" +
                "      lg_name:\n" +
                "        - LG1\n" +
                "  scheduler:\n" +
                "    rampup: 0\n" +
                "    duration: 300\n";

        Assert.assertEquals("42", proxy.createOrUpdateTestFromYamlTest(yaml).getID());
    }

    @Test
    public void testObjectAndUtilityMethods_areCovered() throws Exception {
        StubPcRestProxy proxy = new StubPcRestProxy();
        proxy.registerResponse("GET", "/tests/11", "<Test><ID>11</ID><Name>myTest</Name><TestFolderPath>Subject\\folder</TestFolderPath></Test>");
        proxy.registerResponse("PUT", "/tests/11", "<ok/>");
        proxy.registerResponse("DELETE", "/tests/11", "<ok/>");

        com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test test = proxy.getTest(11);
        Assert.assertEquals("11", test.getID());

        Content content = new Content();
        Assert.assertEquals("11", proxy.updateTest(11, content).getID());
        Assert.assertTrue(proxy.deleteTest(11));

        Assert.assertEquals(15, proxy.extractTestIdFromString("conflict ID:'15' occurred"));
        Assert.assertEquals("localhost", proxy.GetPcServer());
        Assert.assertEquals("", proxy.GetTenant());

        Content parsedContent = proxy.readYaml("controller: local-controller\n");
        Assert.assertEquals("local-controller", parsedContent.getController());

        Content fromStatic = PcRestProxy.getContentFromXmlOrYamlString("<Content><Controller>api</Controller></Content>");
        Assert.assertNotNull(fromStatic);
    }

    private static class StubPcRestProxy extends PcRestProxy {
        private final List<ResponseRule> responses = new ArrayList<ResponseRule>();
        private final List<RawResponseRule> rawResponses = new ArrayList<RawResponseRule>();

        StubPcRestProxy() throws PcException {
            this(false);
        }

        StubPcRestProxy(boolean authenticateWithToken) throws PcException {
            super("http", "localhost", authenticateWithToken, "DANIEL", "proj1", "", "", "");
        }

        void registerResponse(String method, String urlContains, String body) {
            responses.add(new ResponseRule(method, urlContains, body));
        }

        void registerRawResponse(String method, String urlContains, byte[] body) {
            rawResponses.add(new RawResponseRule(method, urlContains, body));
        }

        @Override
        protected String executeRequest(HttpRequestBase request) {
            String url = request.getURI().toString();
            ResponseRule bestMatch = null;
            for (ResponseRule rule : responses) {
                if (rule.matches(request.getMethod(), url)) {
                    if (bestMatch == null || rule.urlContains.length() > bestMatch.urlContains.length()) {
                        bestMatch = rule;
                    }
                }
            }
            if (bestMatch != null) {
                return bestMatch.body;
            }
            throw new AssertionError("No mocked response for request: " + request.getMethod() + " " + url);
        }

        @Override
        protected CloseableHttpResponse executeRawRequest(HttpRequestBase request) throws IOException {
            String url = request.getURI().toString();
            RawResponseRule bestMatch = null;
            for (RawResponseRule rule : rawResponses) {
                if (rule.matches(request.getMethod(), url)) {
                    if (bestMatch == null || rule.urlContains.length() > bestMatch.urlContains.length()) {
                        bestMatch = rule;
                    }
                }
            }
            if (bestMatch != null) {
                CloseableHttpResponse response = Mockito.mock(CloseableHttpResponse.class);
                Mockito.when(response.getEntity()).thenReturn(new ByteArrayEntity(bestMatch.body));
                return response;
            }
            throw new AssertionError("No mocked raw response for request: " + request.getMethod() + " " + url);
        }
    }

    private static class ResponseRule {
        private final String method;
        private final String urlContains;
        private final String body;

        private ResponseRule(String method, String urlContains, String body) {
            this.method = method;
            this.urlContains = urlContains;
            this.body = body;
        }

        private boolean matches(String requestMethod, String url) {
            return method.equalsIgnoreCase(requestMethod) && url.contains(urlContains);
        }
    }

    private static class RawResponseRule {
        private final String method;
        private final String urlContains;
        private final byte[] body;

        private RawResponseRule(String method, String urlContains, byte[] body) {
            this.method = method;
            this.urlContains = urlContains;
            this.body = body;
        }

        private boolean matches(String requestMethod, String url) {
            return method.equalsIgnoreCase(requestMethod) && url.contains(urlContains);
        }
    }
}
