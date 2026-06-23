package com.microfocus.adm.performancecenter.plugins.common.rest;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.*;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.Content;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public interface PcRestProxyClient {
    boolean authenticate(String userName, String password) throws PcException, IOException;

    PcRunResponse startRun(int testId,
                           int testInstanceId,
                           TimeslotDuration timeslotDuration,
                           String postRunAction,
                           boolean vudsMode,
                           int timeslot) throws PcException, IOException;

    int createTestInstance(int testId, int testSetId) throws PcException, IOException;

    @Deprecated
    PcTestSets GetAllTestSets() throws IOException, PcException;

    PcTestSets getAllTestSets() throws IOException, PcException;

    PcTestInstances getTestInstancesByTestId(int testId) throws PcException, IOException;

    boolean stopRun(int runId, String stopMode) throws PcException, IOException;

    PcRunResponse getRunData(int runId) throws PcException, IOException;

    PcTest getTestData(int testId) throws IOException, PcException;

    PcRunResults getRunResults(int runId) throws PcException, IOException;

    boolean GetRunResultData(int runId, int resultId, String localFilePath) throws PcException, IOException;

    TrendReportTransactionDataRoot getTrendReportByXML(String trendReportId, int runId) throws PcException, IOException;

    boolean updateTrendReport(String trendReportId, TrendReportRequest trendReportRequest) throws PcException, IOException;

    InputStream getTrendingPDF(String trendReportId) throws IOException, PcException;

    ArrayList<PcTrendedRun> getTrendReportMetaData(String trendReportId) throws PcException, IOException;

    PcRunEventLog getRunEventLog(int runId) throws PcException, IOException;

    Timeslots GetOpenTimeslotsByTestId(int testId) throws PcException, IOException;

    boolean logout() throws PcException, IOException;

    PcScripts getScripts() throws IOException, PcException;

    PcScript getScript(int Id) throws IOException, PcException;

    PcScript getScript(String testFolderPath, String scriptName) throws IOException, PcException;

    int uploadScript(String testFolderPath, boolean overwrite, boolean runtimeOnly, boolean keepCheckedOut, String scriptPath)
            throws PcException, IOException;

    boolean deleteScript(int scriptId) throws PcException, IOException;

    PcTestSetFolders getTestSetFolders() throws IOException, PcException;

    PcTestSetFolder createTestSetFolder(int parentId, String name) throws IOException, PcException;

    PcTestSet createTestSet(String testSetName, int testSetParentId, String testSetComment) throws IOException, PcException;

    default PcTestSet createTestSet(String testSetName, int testSetParentId) throws IOException, PcException {
        return createTestSet(testSetName, testSetParentId, "");
    }

    int addTrendReport(String name, String description) throws IOException, PcException;

    default int addTrendReport(String name) throws IOException, PcException {
        return addTrendReport(name, "");
    }

    PcTestPlanFolders getTestPlanFolders() throws IOException, PcException;

    boolean verifyTestPlanFolderExist(String path) throws IOException, PcException;

    PcTestPlanFolder createTestPlanFolder(String existingPath, String name) throws IOException, PcException;

    ArrayList<PcTestPlanFolder> createTestPlanFolders(String[] paths) throws IOException, PcException;

    ArrayList<PcTestPlanFolder> createPcTestPlanFolders(ArrayList<String[]> stringsOfExistingPathFromSubjectAndOfFolderToCreate)
            throws IOException, PcException;

    Test createOrUpdateTestFromYamlTest(String testString) throws IOException, PcException;

    Test createOrUpdateTestFromYamlContent(String testName, String testFolderPath, String testOrContent) throws IOException, PcException;

    Test createOrUpdateTest(String testName, String testFolderPath, String xml) throws IOException, PcException;

    Test createOrUpdateTest(String testName, String testFolderPath, Content content) throws IOException, PcException;

    Test getTest(int testId) throws IOException, PcException;

    Test updateTest(int testId, Content content) throws IOException, PcException;

    boolean deleteTest(int testId) throws IOException, PcException;

    int extractTestIdFromString(String value);

    Content readYaml(String yamlContent) throws IOException;

    String GetPcServer();

    String GetTenant();
}
