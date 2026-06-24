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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.*;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.Content;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.SimplifiedTest;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent;
import com.microfocus.adm.performancecenter.plugins.common.utils.Base64Encoder;
import com.microfocus.adm.performancecenter.plugins.common.utils.ConvertContentStringToTest;
import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.HttpStatus.*;

public class PcRestProxy implements PcRestProxyClient {

    public static final String PC_API_XMLNS = "http://www.hp.com/PC/REST/API";
    protected static final String BASE_PC_API_URL = "%s://%s/LoadTest/rest";
    protected static final String BASE_PC_API_AUTHENTICATION_URL = BASE_PC_API_URL + "/authentication-point";
    protected static final String AUTHENTICATION_LOGIN_URL = BASE_PC_API_AUTHENTICATION_URL + "/authenticate";
    protected static final String AUTHENTICATION_WITH_TOKEN_LOGIN_URL = BASE_PC_API_AUTHENTICATION_URL + "/authenticateclient";
    protected static final String AUTHENTICATION_LOGOUT_URL = BASE_PC_API_AUTHENTICATION_URL + "/logout";
    protected static final String PC_API_RESOURCES_TEMPLATE = BASE_PC_API_URL + "/domains/%s/projects/%s";
    protected static final String RUNS_RESOURCE_NAME = "Runs";
    protected static final String TESTS_RESOURCE_NAME = "tests";
    protected static final String TEST_INSTANCES_NAME = "testinstances";
    protected static final String TEST_SETS_NAME = "testsets";
    protected static final String RESULTS_RESOURCE_NAME = "Results";
    protected static final String EVENTLOG_RESOURCE_NAME = "EventLog";
    protected static final String TREND_REPORT_RESOURCE_NAME = "TrendReports";
    protected static final String TREND_REPORT_RESOURCE_SUFFIX = "data";
    protected static final String CONTENT_TYPE_XML = "application/xml";
    protected static final String SCRIPTS_RESOURCE_NAME = "Scripts";
    protected static final String TESTPLAN_RESOURCE_NAME = "testplan";
    protected static final String TEST_SET_FOLDERS_RESOURCE_NAME = "testsetfolders";
    protected static final String TIMESLOTS = "timeslots";
    protected static final List<Integer> validStatusCodes = Arrays.asList(SC_OK, SC_CREATED, SC_ACCEPTED, SC_NO_CONTENT);
    private final String baseURL;
    private final String pcServer;
    private final String webProtocol;
    private String proxyScheme;
    private String proxyHostName;
    private int proxyPort;
    private final CloseableHttpClient client;
    private final CookieStore cookieStore;
    private final String tenantSuffix;
    private final boolean authenticateWithToken;

    public PcRestProxy(
            String webProtocolName,
            String pcServerName,
            boolean authenticateWithToken,
            String almDomain,
            String almProject,
            String proxyOutURL,
            String proxyUser,
            String proxyPassword) throws PcException {
        String[] lreServerAndTenant = Helper.getLreServerAndTenant(pcServerName);
        this.pcServer = lreServerAndTenant[0];
        this.tenantSuffix = lreServerAndTenant[1];
        this.webProtocol = webProtocolName;
        this.baseURL = String.format(PC_API_RESOURCES_TEMPLATE, this.webProtocol, this.pcServer, almDomain, almProject);
        this.authenticateWithToken = authenticateWithToken;

        // Connection manager replacement
        PoolingHttpClientConnectionManager cxMgr = new PoolingHttpClientConnectionManager();
        cxMgr.setMaxTotal(100);
        cxMgr.setDefaultMaxPerRoute(20);

        HttpClientBuilder builder = HttpClientBuilder.create()
                .setConnectionManager(cxMgr);

        // Use the RFC 6265 cookie policy so that session-deletion cookies (e.g. the
        // LWSSO logout cookie with "expires=Thu, 01 Jan 1970 00:00:00 GMT") are parsed
        // without emitting "Invalid cookie header" warnings from the legacy date parser.
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD);

        // Proxy setup
        if (proxyOutURL != null && !proxyOutURL.isEmpty()) {
            getProxyDataFromURL(proxyOutURL);
            HttpHost proxy = new HttpHost(this.proxyHostName, this.proxyPort, this.proxyScheme);

            requestConfigBuilder.setProxy(proxy);

            if (proxyUser != null && !proxyUser.isEmpty()) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(this.proxyHostName, this.proxyPort),
                        new UsernamePasswordCredentials(proxyUser, proxyPassword)
                );
                builder.setDefaultCredentialsProvider(credsProvider);
            }
        }
        builder.setDefaultRequestConfig(requestConfigBuilder.build());
        this.client = builder.build();

        this.cookieStore = new BasicCookieStore();
    }

    /**
     * Builds a fresh per-request {@link HttpContext} backed by the shared, thread-safe
     * {@link CookieStore}. A new context is created for every request so that a single
     * {@code PcRestProxy} instance can safely be used concurrently from multiple threads,
     * while all threads continue to share the same authenticated session cookies.
     */
    private HttpContext createContext() {
        HttpClientContext ctx = HttpClientContext.create();
        ctx.setCookieStore(cookieStore);
        return ctx;
    }

    public static Content getContentFromXmlOrYamlString(String xmlOrYamlTest) {
        Content content = null;
        try {
            Test test = Test.xmlToObject(xmlOrYamlTest);
            content = test.getContent();
        } catch (Exception ex) {
            content = Content.xmlToObject(xmlOrYamlTest);
        }
        return content;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isOk(HttpResponse response) {
        return validStatusCodes.contains(response.getStatusLine().getStatusCode());
    }

    public static SimplifiedContent xmlOrYamlStringToSimplifiedContent(String strSimplifiedContent) throws IOException {
        if (strSimplifiedContent == null || strSimplifiedContent.isEmpty())
            return null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimplifiedContent simplifiedContent;
        try {
            simplifiedContent = mapper.readValue(strSimplifiedContent, SimplifiedContent.class);
        } catch (Exception ex1) {
            try {
                simplifiedContent = SimplifiedContent.xmlToObject(strSimplifiedContent);
            } catch (Exception ex2) {
                throw ex1;
            }
        }
        return simplifiedContent;
    }

    public static SimplifiedTest xmlOrYamlStringToSimplifiedTest(String strSimplifiedTest) throws IOException {
        if (strSimplifiedTest == null || strSimplifiedTest.isEmpty())
            return null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SimplifiedTest simplifiedTest;
        try {
            simplifiedTest = mapper.readValue(strSimplifiedTest, SimplifiedTest.class);
        } catch (Exception ex1) {
            try {
                simplifiedTest = SimplifiedTest.xmlToObject(strSimplifiedTest);
            } catch (Exception ex2) {
                throw ex1;
            }
        }
        return simplifiedTest;
    }

    private void getProxyDataFromURL(String proxyURL) throws PcException {
        try {
            String mainStr = "";
            if (proxyURL != null && !proxyURL.isEmpty()) {
                String[] urlSplit = proxyURL.split("://");

                proxyScheme = urlSplit[0];
                mainStr = urlSplit[1];
                if (mainStr.contains(":")) {
                    proxyHostName = mainStr.split(":")[0];
                    proxyPort = Integer.parseInt(mainStr.split(":")[1]);
                } else {
                    proxyHostName = mainStr;
                    proxyPort = 80;
                }
            }
        } catch (Exception ex) {
            throw new PcException("Error: Validating Proxy URL: " + ex + " Please add a proxy URL in this pattern: http(s)://<host>:<port> or leave blank");
        }
    }

    public boolean authenticate(String userName, String password) throws PcException, ClientProtocolException, IOException {
        if (this.authenticateWithToken)
            return authenticateWithToken(userName, password);
        else
            return authenticateWithoutToken(userName, password);
    }

    private boolean authenticateWithoutToken(String userName, String password) throws PcException, ClientProtocolException, IOException {
        String userNameAndPassword = userName + ":" + password;
        String encodedCredentials = Base64Encoder.encode(userNameAndPassword.getBytes());
        HttpGet authRequest = new HttpGet(String.format(AUTHENTICATION_LOGIN_URL + tenantSuffix, webProtocol, pcServer));
        authRequest.addHeader("Authorization", String.format("Basic %s", encodedCredentials));
        executeRequest(authRequest);
        return true;
    }

    private boolean authenticateWithToken(String idKey, String secretKey) throws PcException, ClientProtocolException, IOException {
        AuthenticationClient authenticationClient = new AuthenticationClient(idKey, secretKey);
        HttpPost authRequestWithToken = new HttpPost(String.format(AUTHENTICATION_WITH_TOKEN_LOGIN_URL + tenantSuffix, webProtocol, pcServer));
        authRequestWithToken.addHeader(RESTConstants.CONTENT_TYPE, CONTENT_TYPE_XML);
        authRequestWithToken.setEntity(new StringEntity(authenticationClient.objectToXML(), org.apache.http.entity.ContentType.APPLICATION_XML));
        executeRequest(authRequestWithToken);
        return true;
    }

    public PcRunResponse startRun(
            int testId,
            int testInstanceId,
            TimeslotDuration timeslotDuration,
            String postRunAction,
            boolean vudsMode,
            int timeslot
    ) throws PcException, IOException {

        HttpPost request = new HttpPost(String.format(baseURL + "/%s", RUNS_RESOURCE_NAME));
        request.addHeader(RESTConstants.CONTENT_TYPE, CONTENT_TYPE_XML);

        PcRunRequest runRequestData = new PcRunRequest(
                testId, testInstanceId, timeslot, timeslotDuration, postRunAction, vudsMode
        );

        // Set XML payload
        request.setEntity(new StringEntity(
                runRequestData.objectToXML(),
                org.apache.http.entity.ContentType.APPLICATION_XML
        ));

        // executeRequest returns the response body as a String
        String responseXml = executeRequest(request);

        // Convert XML to object
        return PcRunResponse.xmlToObject(responseXml);
    }


    public int createTestInstance(int testId, int testSetId) throws PcException, IOException {
        HttpPost request = new HttpPost(String.format(baseURL + "/%s", TEST_INSTANCES_NAME));

        TestInstanceCreateRequest createRequest = new TestInstanceCreateRequest(testId, testSetId);

        // Set XML payload
        request.setEntity(new StringEntity(
                createRequest.objectToXML(),
                org.apache.http.entity.ContentType.APPLICATION_XML
        ));
        request.addHeader(RESTConstants.CONTENT_TYPE, CONTENT_TYPE_XML);

        // executeRequest returns response body as a String
        String responseXml = executeRequest(request);

        int testInstanceID;
        try {
            testInstanceID = createRequest.getTestInstanceIDFromResponse(responseXml, "TestInstanceID");
        } catch (SAXException | ParserConfigurationException e) {
            throw new PcException("createTestInstance exception: " + e, e);
        }

        return testInstanceID;
    }

    @Deprecated
    public PcTestSets GetAllTestSets() throws IOException, PcException {
        // Internally use the new executeRequest returning String
        return getAllTestSets();
    }

    public PcTestSets getAllTestSets() throws IOException, PcException {
        String getTestSetsUrl = String.format(baseURL + "/%s", TEST_SETS_NAME);
        HttpGet request = new HttpGet(getTestSetsUrl);
        String testSetsXml = executeRequest(request);
        return PcTestSets.xmlToObject(testSetsXml);
    }

    public PcTestInstances getTestInstancesByTestId(int testId) throws PcException, IOException {
        String uri = String.format(
                baseURL + "/%s?%s=%s",
                TEST_INSTANCES_NAME,
                "query",
                URLEncoder.encode("{test-id[" + testId + "]}", "UTF-8")
        );

        HttpGet request = new HttpGet(uri);

        // executeRequest now returns the response body as a String
        String testInstancesXml = executeRequest(request);

        // Convert XML to object
        return PcTestInstances.xmlToObject(testInstancesXml);
    }


    public boolean stopRun(int runId, String stopMode) throws PcException, ClientProtocolException, IOException {
        String stopUrl = String.format(baseURL + "/%s/%s/%s", RUNS_RESOURCE_NAME, runId, stopMode);
        HttpPost stopRunRequest = new HttpPost(stopUrl);
        ReleaseTimeslot releaseTimesloteRequest = new ReleaseTimeslot(true, "Do Not Collate");
        stopRunRequest.addHeader(RESTConstants.CONTENT_TYPE, CONTENT_TYPE_XML);
        stopRunRequest.setEntity(new StringEntity(releaseTimesloteRequest.objectToXML(), org.apache.http.entity.ContentType.APPLICATION_XML));
        executeRequest(stopRunRequest);
        return true;
    }

    public PcRunResponse getRunData(int runId) throws PcException, IOException {
        HttpGet getRunDataRequest = new HttpGet(String.format(baseURL + "/%s/%s", RUNS_RESOURCE_NAME, runId));
        String runData = executeRequest(getRunDataRequest);
        return PcRunResponse.xmlToObject(runData);
    }


    public PcTest getTestData(int testId) throws IOException, PcException {
        HttpGet getTestDataRequest = new HttpGet(String.format(baseURL + "/%s/%s", TESTS_RESOURCE_NAME, testId));
        String runData = executeRequest(getTestDataRequest);
        return PcTestData.xmlToObject(runData);
    }

    public PcRunResults getRunResults(int runId) throws PcException, ClientProtocolException, IOException {
        String getRunResultsUrl = String
                .format(baseURL + "/%s/%s/%s", RUNS_RESOURCE_NAME, runId, RESULTS_RESOURCE_NAME);
        HttpGet getRunResultsRequest = new HttpGet(getRunResultsUrl);
        String runData = executeRequest(getRunResultsRequest);
        return PcRunResults.xmlToObject(runData);
    }

    public boolean GetRunResultData(int runId, int resultId, String localFilePath)
            throws PcException, IOException {

        String getRunResultDataUrl = String.format(
                baseURL + "/%s/%s/%s/%s/data",
                RUNS_RESOURCE_NAME, runId, RESULTS_RESOURCE_NAME, resultId);

        HttpGet getRunResultRequest = new HttpGet(getRunResultDataUrl);

        // Use the raw request method
        try (CloseableHttpResponse response = executeRawRequest(getRunResultRequest);
             InputStream in = response.getEntity().getContent();
             OutputStream out = Files.newOutputStream(Paths.get(localFilePath))) {

            IOUtils.copy(in, out); // copy the content safely
            return true;
        }
    }

    public TrendReportTransactionDataRoot getTrendReportByXML(String trendReportId, int runId)
            throws PcException, IOException {

        String url = String.format(baseURL + "/%s/%s/%s",
                TREND_REPORT_RESOURCE_NAME, trendReportId, runId);

        HttpGet request = new HttpGet(url);

        // executeRequest now returns the response body as a String
        String trendReportByXML = executeRequest(request);

        // convert XML to object
        return TrendReportTransactionDataRoot.xmlToObject(trendReportByXML);
    }

    public boolean updateTrendReport(String trendReportId, TrendReportRequest trendReportRequest) throws PcException, IOException {
        String updateTrendReportUrl = String.format(baseURL + "/%s/%s", TREND_REPORT_RESOURCE_NAME, trendReportId);
        HttpPost updateTrendReportRequest = new HttpPost(updateTrendReportUrl);
        updateTrendReportRequest.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_XML);
        updateTrendReportRequest.setEntity(new StringEntity(trendReportRequest.objectToXML(), org.apache.http.entity.ContentType.APPLICATION_XML));
        executeRequest(updateTrendReportRequest);
        return true;
    }

    public InputStream getTrendingPDF(String trendReportId) throws IOException, PcException {
        String getTrendReportUrl = String.format(
                baseURL + "/%s/%s/%s",
                TREND_REPORT_RESOURCE_NAME, trendReportId, TREND_REPORT_RESOURCE_SUFFIX);

        HttpGet getTrendReportRequest = new HttpGet(getTrendReportUrl);

        CloseableHttpResponse response = executeRawRequest(getTrendReportRequest);
        // Caller must close response after consuming InputStream
        InputStream rawStream =  response.getEntity().getContent();
        return new FilterInputStream(rawStream) {
            @Override
            public void close() throws IOException {
                super.close();
                response.close(); // ensures HTTP connection is released
            }
        };
    }

    public ArrayList<PcTrendedRun> getTrendReportMetaData(String trendReportId) throws PcException, ClientProtocolException, IOException {
        String getTrendReportMetaDataUrl = String.format(baseURL + "/%s/%s", TREND_REPORT_RESOURCE_NAME, trendReportId);
        HttpGet getTrendReportMetaDataRequest = new HttpGet(getTrendReportMetaDataUrl);
        String trendReportMetaData = executeRequest(getTrendReportMetaDataRequest);
        return PcTrendReportMetaData.xmlToObject(trendReportMetaData);
    }

    public PcRunEventLog getRunEventLog(int runId) throws PcException, ClientProtocolException, IOException {
        String getRunEventLogUrl = String
                .format(baseURL + "/%s/%s/%s", RUNS_RESOURCE_NAME, runId, EVENTLOG_RESOURCE_NAME);
        HttpGet getRunEventLogRequest = new HttpGet(getRunEventLogUrl);
        String runEventLog = executeRequest(getRunEventLogRequest);
        return PcRunEventLog.xmlToObject(runEventLog);
    }

    public Timeslots GetOpenTimeslotsByTestId(int testId) throws PcException, IOException {
        String uri = String.format(baseURL + "/%s?%s=%s", TIMESLOTS, "query", URLEncoder.encode("{LoadTestID[" + testId + "]}", "UTF-8"));
        HttpGet getTimeslotsByTestID = new HttpGet(uri);
        String allTimeslots = executeRequest(getTimeslotsByTestID);
        Timeslots timeslots = Timeslots.xmlToObject(allTimeslots);
        ArrayList<Timeslot> openedTimeslots = timeslots.getTimeslotsList().stream().filter(timeslot -> "open".equalsIgnoreCase(timeslot.getOpenStatus())).collect(Collectors.toCollection(ArrayList::new));
        timeslots.setTimeslotsList(openedTimeslots);
        return timeslots;
    }

    public boolean logout() throws PcException, ClientProtocolException, IOException {
        HttpGet logoutRequest = new HttpGet(String.format(AUTHENTICATION_LOGOUT_URL, webProtocol, pcServer));
        executeRequest(logoutRequest);
        return true;
    }

    protected String executeRequest(HttpRequestBase request) throws PcException, IOException {
        try (CloseableHttpResponse response = client.execute(request, createContext())) {
            if (!isOk(response)) {
                String message;
                try {
                    String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    PcErrorResponse exception = PcErrorResponse.xmlToObject(content);
                    message = String.format("%s Error code: %s", exception.getExceptionMessage(), exception.getErrorCode());
                } catch (Exception ex) {
                    message = response.getStatusLine().toString();
                }
                throw new PcException("executeRequest exception: " + message);
            }
            // Return body as string (or parse it into your model here)
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }

    protected CloseableHttpResponse executeRawRequest(HttpRequestBase request) throws PcException, IOException {
        CloseableHttpResponse response = client.execute(request, createContext());
        if (!isOk(response)) {
            try {
                String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                PcErrorResponse exception = PcErrorResponse.xmlToObject(content);
                String message = String.format("%s Error code: %s", exception.getExceptionMessage(), exception.getErrorCode());
                throw new PcException("executeRequest exception: " + message);
            } catch (Exception ex) {
                throw new PcException("executeRequest exception: " + response.getStatusLine());
            } finally {
                response.close(); // important
            }
        }
        return response; // caller must close it
    }

    public PcScripts getScripts() throws IOException, PcException {
        HttpGet getScriptsRequest = new HttpGet(String.format(baseURL + "/%s", SCRIPTS_RESOURCE_NAME));
        String scripts = executeRequest(getScriptsRequest);
        return PcScripts.xmlToObject(scripts);
    }

    public PcScript getScript(int Id) throws IOException, PcException {
        HttpGet getScriptsRequest = new HttpGet(String.format(baseURL + "/%s/%s", SCRIPTS_RESOURCE_NAME, Id));
        String script = executeRequest(getScriptsRequest);
        return PcScript.xmlToObject(script);
    }

    public PcScript getScript(String testFolderPath, String scriptName) throws IOException, PcException {
        List<PcScript> pcScriptList = getScripts().getPcScriptList();
        for (PcScript pcScript : pcScriptList
        ) {
            if (pcScript.getTestFolderPath().equalsIgnoreCase(testFolderPath) && pcScript.getName().equalsIgnoreCase(scriptName)) {
                return pcScript;
            }
        }
        throw new PcException(String.format("No script named '%s' was found under this folder path '%s' within the PC project.", scriptName, testFolderPath));
    }

    public int uploadScript(String testFolderPath, boolean overwrite, boolean runtimeOnly, boolean keepCheckedOut, String scriptPath)
            throws PcException, IOException {

        // Ensure Test Plan folder exists
        createTestPlanFolder(testFolderPath);

        // Prepare HTTP POST
        HttpPost createScriptRequest = new HttpPost(String.format(baseURL + "/%s", SCRIPTS_RESOURCE_NAME));

        File fileToSend = new File(scriptPath);
        ScriptCreateRequest scriptCreateRequest = new ScriptCreateRequest(testFolderPath, overwrite, runtimeOnly, keepCheckedOut);

        // Build multipart entity
        MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("File", fileToSend, ContentType.APPLICATION_OCTET_STREAM, fileToSend.getName())
                .addTextBody("Text", scriptCreateRequest.objectToXML(), ContentType.TEXT_PLAIN);

        createScriptRequest.setEntity(builder.build());

        // Execute request and get response as String
        String responseXml = executeRequest(createScriptRequest);

        int scriptID;
        try {
            scriptID = scriptCreateRequest.getScriptIdFromResponse(responseXml, "ID");
        } catch (SAXException | ParserConfigurationException e) {
            throw new PcException("uploadScript exception: " + e, e);
        }

        return scriptID;
    }

    public boolean deleteScript(int scriptId) throws PcException, ClientProtocolException, IOException {
        HttpDelete deleteRequest = new HttpDelete(String.format(baseURL + "/%s/%s", SCRIPTS_RESOURCE_NAME, scriptId));
        executeRequest(deleteRequest);
        return true;
    }

    public PcTestSetFolders getTestSetFolders() throws IOException, PcException {
        HttpGet request = new HttpGet(String.format(baseURL + "/%s", TEST_SET_FOLDERS_RESOURCE_NAME));
        String responseXml = executeRequest(request);
        return PcTestSetFolders.xmlToObject(responseXml);
    }

    public PcTestSetFolder createTestSetFolder(int parentId, String name) throws IOException, PcException {
        HttpPost request = new HttpPost(String.format(baseURL + "/%s", TEST_SET_FOLDERS_RESOURCE_NAME));
        TestSetFolderCreateRequest createRequest = new TestSetFolderCreateRequest(name, parentId);
        request.setEntity(new StringEntity(createRequest.objectToXML(), org.apache.http.entity.ContentType.APPLICATION_XML));
        request.addHeader(RESTConstants.CONTENT_TYPE, CONTENT_TYPE_XML);

        String responseXml = executeRequest(request);
        return createRequest.getPcTestSetFolderFromResponse(responseXml);
    }

    public PcTestSet createTestSet(String testSetName, int testSetParentId) throws IOException, PcException {
        return createTestSet(testSetName, testSetParentId, "");
    }

    public PcTestSet createTestSet(String testSetName, int testSetParentId, String testSetComment) throws IOException, PcException {
        HttpPost request = new HttpPost(String.format(baseURL + "/%s", TEST_SETS_NAME));
        TestSetCreateRequest createRequest = new TestSetCreateRequest(testSetName, testSetParentId, testSetComment);
        request.setEntity(new StringEntity(createRequest.objectToXML(), org.apache.http.entity.ContentType.APPLICATION_XML));
        request.addHeader(RESTConstants.CONTENT_TYPE, CONTENT_TYPE_XML);

        String responseXml = executeRequest(request);
        return createRequest.getPcTestSetFromResponse(responseXml);
    }

    public int addTrendReport(String name) throws IOException, PcException {
        return addTrendReport(name, "");
    }

    public int addTrendReport(String name, String description) throws IOException, PcException {
        HttpPost request = new HttpPost(String.format(baseURL + "/%s", TREND_REPORT_RESOURCE_NAME));
        TrendReportCreateRequest createRequest = new TrendReportCreateRequest(name, description);
        request.setEntity(new StringEntity(createRequest.objectToXML(), org.apache.http.entity.ContentType.APPLICATION_XML));
        request.addHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_XML);

        String responseXml = executeRequest(request);
        try {
            return createRequest.getTrendReportIdFromResponse(responseXml);
        } catch (SAXException | ParserConfigurationException e) {
            throw new PcException("addTrendReport exception: " + e, e);
        }
    }

    public PcTestPlanFolders getTestPlanFolders() throws IOException, PcException {
        HttpGet getFolderTreeRequest = new HttpGet(String.format(baseURL + "/%s", TESTPLAN_RESOURCE_NAME));
        String testPlan = executeRequest(getFolderTreeRequest);
        return PcTestPlanFolders.xmlToObject(testPlan);
    }

    public boolean verifyTestPlanFolderExist(String path) throws IOException, PcException {
        PcTestPlanFolders pcTestPlanFolders = getTestPlanFolders();
        if (pcTestPlanFolders != null) {
            for (PcTestPlanFolder pcTestPlanFolder : pcTestPlanFolders.getPcTestPlanFolderList()
            ) {
                if (pcTestPlanFolder.getFullPath().equals(path))
                    return true;
            }
        }
        return false;
    }

    public PcTestPlanFolder createTestPlanFolder(String existingPath, String name) throws IOException, PcException {
        HttpPost request = new HttpPost(String.format(baseURL + "/%s", TESTPLAN_RESOURCE_NAME));

        TestPlanFolderCreateRequest testPlanFolderCreateRequest = new TestPlanFolderCreateRequest(existingPath, name);
        request.setEntity(new StringEntity(
                testPlanFolderCreateRequest.objectToXML(),
                org.apache.http.entity.ContentType.APPLICATION_XML
        ));
        request.addHeader(RESTConstants.CONTENT_TYPE, CONTENT_TYPE_XML);

        String responseXml = executeRequest(request);

        try {
            return testPlanFolderCreateRequest.getPcTestPlanFolderFromResponse(responseXml);
        } catch (SAXException | ParserConfigurationException e) {
            throw new PcException("createTestPlanFolder exception: " + e, e);
        }
    }

    public ArrayList<PcTestPlanFolder> createTestPlanFolders(String[] paths) throws IOException, PcException {
        ArrayList<String[]> pathFromSubjectAndFolders = Helper.getArrayListOfStringArray(paths);
        PcTestPlanFolders pcTestPlanFolders = getTestPlanFolders();
        ArrayList<String[]> pathFromSubjectAndFoldersFiltered = Helper.getCleanAndNonExistingAndSortedArrayList(pathFromSubjectAndFolders, pcTestPlanFolders);
        return createPcTestPlanFolders(pathFromSubjectAndFoldersFiltered);
    }

    public ArrayList<PcTestPlanFolder> createPcTestPlanFolders(ArrayList<String[]> stringsOfExistingPathFromSubjectAndOfFolderToCreate) throws IOException, PcException {
        //creating the items from ArrayList not existing in PC
        ArrayList<PcTestPlanFolder> createdPcTestPlanFolders = new ArrayList<PcTestPlanFolder>();
        if (!stringsOfExistingPathFromSubjectAndOfFolderToCreate.isEmpty()) {
            for (String[] pathFromSubjectAndFolder : stringsOfExistingPathFromSubjectAndOfFolderToCreate
            ) {
                PcTestPlanFolder createdPcTestPlanFolder = createTestPlanFolder(pathFromSubjectAndFolder[0], pathFromSubjectAndFolder[1]);
                createdPcTestPlanFolders.add(createdPcTestPlanFolder);
            }
        }
        return createdPcTestPlanFolders;
    }

    public Test createOrUpdateTestFromYamlTest(String testString) throws IOException, PcException {
        SimplifiedTest simplifiedTest = xmlOrYamlStringToSimplifiedTest(testString);
        return createOrUpdateTestFromYamlContent("", "", testString);
    }

    public Test createOrUpdateTestFromYamlContent(String testName, String testFolderPath, String testOrContent) throws IOException, PcException {
        ConvertContentStringToTest.ConversionResult conversionResult =
                new ConvertContentStringToTest(this).convert(testName, testFolderPath, testOrContent);
        return createOrUpdateTest(
                conversionResult.getTestName(),
                conversionResult.getTestFolderPathWithSubject(),
                conversionResult.getContent()
        );
    }

    public Test createOrUpdateTest(String testName, String testFolderPath, String xml) throws IOException, PcException {
        String testFolderPathWithCorrectSeparatorsAndSubject = testFolderPath.replace("/", "\\");
        if (!testFolderPath.startsWith("Subject"))
            testFolderPathWithCorrectSeparatorsAndSubject = "Subject\\".concat(testFolderPath);
        Content content = getContentFromXmlOrYamlString(xml);
        return createOrUpdateTest(testName, testFolderPathWithCorrectSeparatorsAndSubject, content);
    }

    public Test createOrUpdateTest(String testName, String testFolderPath, Content content) throws IOException, PcException {
        createTestPlanFolder(testFolderPath);
        try {
            HttpPost createTestRequest = new HttpPost(String.format(baseURL + "/%s", TESTS_RESOURCE_NAME));
            Test test = new Test(testName, testFolderPath, content);
            createTestRequest.setEntity(new StringEntity(test.objectToXML(), org.apache.http.entity.ContentType.APPLICATION_XML));
            String createTestResponse = executeRequest(createTestRequest); // now returns String
            return Test.xmlToObject(createTestResponse);
        } catch (PcException e) { //in case of failure, verify if exception return conflict with exiting test
            int testId = extractTestIdFromString(e.getMessage());
            if (testId != 0) {
                return updateTest(testId, content);
            }
            throw e;
        }
    }

    private void createTestPlanFolder(String testFolderPath) {
        try {
            if (!verifyTestPlanFolderExist(testFolderPath)) {
                String[] scriptPathArray = {testFolderPath};
                createTestPlanFolders(scriptPathArray);
            }
        } catch (PcException | IOException ex) {
            //Continuation should be allowed as exist verification and creation was introduced in PC12.60
        }
    }

    public Test getTest(int testId) throws IOException, PcException {
        HttpGet getTestRequest = new HttpGet(String.format(baseURL + "/%s/%s", TESTS_RESOURCE_NAME, testId));
        String xmlTest = executeRequest(getTestRequest);
        return Test.xmlToObject(xmlTest);
    }

    public Test updateTest(int testId, Content content) throws IOException, PcException {
        HttpPut updateTestRequest = new HttpPut(String.format(baseURL + "/%s/%s", TESTS_RESOURCE_NAME, testId));
        updateTestRequest.setEntity(new StringEntity(content.objectToXML(true), org.apache.http.entity.ContentType.APPLICATION_XML));
        executeRequest(updateTestRequest);
        return getTest(testId);
    }

    public boolean deleteTest(int testId) throws IOException, PcException {
        HttpDelete deleteTestRequest = new HttpDelete(String.format(baseURL + "/%s/%s", TESTS_RESOURCE_NAME, testId));
        executeRequest(deleteTestRequest);
        return true;
    }

    protected String getBaseURL() {
        return baseURL;
    }

    public int extractTestIdFromString(String value) {
        return Helper.extractTestIdFromString(value);
    }

    public Content readYaml(String yamlContent) throws IOException {
        Content content = null;
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
        try {
            Test test = mapper.readValue(yamlContent, Test.class);
            content = test.getContent();
        } catch (IOException ex) {
            content = mapper.readValue(yamlContent, Content.class);
        }
        return content;
    }

    public String GetPcServer() {
        return pcServer;
    }

    public String GetTenant() {
        return tenantSuffix;
    }
    //endregion
}
