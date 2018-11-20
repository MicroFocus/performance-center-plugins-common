package com.microfocus.adm.performancecenter.plugins.common.rest;

//import com.microfocus.adm.performancecenter.plugins.common.pcentities.RunState;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.*;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.Content;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.SimplifiedTest;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.common.Common.stringToInteger;
import static org.apache.commons.lang.builder.ToStringStyle.MULTI_LINE_STYLE;

public class TestPcRestProxy {


    //region intro
    private PcRestProxy pcRestProxy;

    public final String RESOURCES_DIR = getClass().getResource("").getPath();

//    @BeforeClass
    public void setUp() throws  PcException, Exception {
        System.out.println("setUp: starts");
        try {
            pcRestProxy = new PcRestProxy(PcRestProxyBase.WEB_PROTOCOL, PcRestProxyBase.PC_SERVER_NAME, PcRestProxyBase.ALM_DOMAIN, PcRestProxyBase.ALM_PROJECT, "","","");
            System.out.println("setUp: setUp done.");
        } catch (PcException ex) {
            System.out.println("setUp: setUp failed. PcException = " + ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        System.out.println("setUp: ends");
    }


    //@Test
    public void verifyAll() throws  PcException, Exception {
        System.out.println("verify starts");
        try {
            setUp();
            testLogin();
            getTestInstancesByTestId(9);
            getTestPlanFolders();
            PcTestPlanFolder pcTestPlanFolder = CreateTestPlanFolder("Subject\\coucou", "scripts2");
            verifyTestPlanFolderExist("Subject\\1coucou1");
            String[] subjectPaths = {"Subject\\1coucou1\\coucou1\\coucou1", "Subject\\1coucou1\\coucou1\\coucou2", "Subject\\1coucou2\\coucou2\\coucou1", "Subject\\1coucou2\\coucou2\\coucou2", "Subject\\1coucou2\\coucou2\\coucou2"};
            createTestPlanFolders(subjectPaths);
            testGetAllTestSets();
            int scriptId = testUploadScript();
            getScripts();
            PcScript pcScript = getScript(scriptId);
            PcScript pcScript2 = getScript(pcScript.getTestFolderPath(), pcScript.getName());
            if(pcScript.getID() == pcScript2.getID())
                System.out.println("both scripts are similar");
            if (scriptId > 0)
                deleteScript(scriptId);
            testLogout();

        } catch (PcException ex) {
            System.out.println("verify failed. PcException = " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("verify failed. Exception = " + ex.getMessage());
        }
        System.out.println("verify ends");
    }
    //endregion

    //region create test
    //@Test
    public void verifyTestCreationOrUpdate() throws  PcException, Exception {
        try {



            setUp();
            testLogin();

            //create the test
            String xmlTestToCreate = fileToStringWithTrim("CreateTest.xml");
            com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test testToCreate =
                    com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test.xmlToObject(xmlTestToCreate);
            com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test createdTest =
                    createOrUpdateTestTest(testToCreate);
            System.out.println(String.format("createdTest - ID=%s, Name=%s, TestFolderPath=%s", createdTest.getID(), createdTest.getName(), createdTest.getTestFolderPath()));
            System.out.println(createdTest.objectToXML());


            //update the test
            String xmlTestToUpdate = fileToStringWithTrim("UpdateTest.xml");
            com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test testToUpdate =
                    com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test.xmlToObject(xmlTestToUpdate);
            com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test updatedTest =
                    createOrUpdateTestTest(testToUpdate);
            System.out.println(String.format("updatedTest - ID=%s, Name=%s, TestFolderPath=%s", updatedTest.getID(), updatedTest.getName(), updatedTest.getTestFolderPath()));
            System.out.println(updatedTest.objectToXML());

            //delete the test
            boolean testDeleted = deleteTest(stringToInteger(updatedTest.getID()));
            System.out.println("test was " + (testDeleted ? "":"not ") + "deleted");

        } catch (Exception e) {
            System.out.println("verifyTestCreation failed. Exception = " + e.getMessage());
            throw e;
        } finally {
            testLogout();
        }
    }

    public static String fileToStringWithTrim(String fileContent) {
        try {
            File fXmlFile = new File("src/test/resources/microfocus/adm/performancecenter/plugins/common/rest/".concat(fileContent));
            //filename is filepath string
            BufferedReader br = new BufferedReader(new FileReader(fXmlFile));
            String line;
            StringBuilder sb = new StringBuilder();
            while((line=br.readLine())!= null){
                sb.append(line.trim().concat(System.getProperty("line.separator")));
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println(String.format("exception: %s", e));
        }
        return "";
    }

    public static String fileToStringWithoutTrim(String fileContent) {
        try {
            File fXmlFile = new File("src/test/resources/microfocus/adm/performancecenter/plugins/common/rest/".concat(fileContent));
            //filename is filepath string
            BufferedReader br = new BufferedReader(new FileReader(fXmlFile));
            String line;
            StringBuilder sb = new StringBuilder();
            while((line=br.readLine())!= null){
                sb.append(line.concat(System.getProperty("line.separator")));
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println(String.format("exception: %s", e));
        }
        return "";
    }

    public com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test createOrUpdateTestTest(com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test test) throws PcException, IOException {
        System.out.println("createOrUpdateTestTest: starts");
        com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test createdOrUpdatedTest = null;

        try {
            String testname = test.getName();
            String folderPath = test.getTestFolderPath();
            Content content = test.getContent();
            createdOrUpdatedTest = pcRestProxy.createOrUpdateTest(testname, folderPath, content);
            return createdOrUpdatedTest;
        } catch (PcException ex) {
            System.out.println("createOrUpdateTestTest: creating or updating test failed. PcException = " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("createOrUpdateTestTest: creating or updating test failed. IOException = " + ex.getMessage());
        }
        System.out.println("createOrUpdateTestTest: ends");
        return createdOrUpdatedTest;
    }

    public com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test updateTest(int testId, com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test test) throws PcException, IOException {
        System.out.println("updateTest: starts");
        com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test testUpdated = null;
        try {
            String testName = test.getName();
            String folderPath = test.getTestFolderPath();
            Content content = test.getContent();
            testUpdated = pcRestProxy.updateTest(testId, content);
            return testUpdated;
        } catch (PcException ex) {
            System.out.println("updateTest: updating test failed. PcException = " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("updateTest: updating test failed. IOException = " + ex.getMessage());
        }
        System.out.println("updateTest: ends");
        return testUpdated;
    }

    public boolean deleteTest(int id) throws PcException, IOException {
        System.out.println("deleteTest: starts");
        try {
            boolean isTestDeleted = pcRestProxy.deleteTest(id);
            return isTestDeleted;
        } catch (PcException ex) {
            System.out.println("deleteTest: deleting test failed. PcException = " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("deleteTest: deleting test failed. IOException = " + ex.getMessage());
        }
        System.out.println("deleteTest: ends");
        return false;
    }

    public int extractTestIdFromStringTest(String value) {
        try {
            String str = "Invalid design performance test request. A performance test (ID:'732323223') named 'testempty2_not_created_by_rest_api_without_SLA' in folder 'Subject\\daniel' already exists.";
            int testID = pcRestProxy.extractTestIdFromString(value);
            return testID;
        } catch (Exception ex) {
            System.out.println("testExtractTestIdFromString: Exception = " + ex.getMessage());
        }
        return 0;
    }


    //endregion

    //region create test from yaml
    //@Test
    public void verifyTestCreationOrUpdateViaXmlOrYaml() throws  PcException, Exception {
        try {

            setUp();
            testLogin();

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());


            //reading CreateTestFromContent.yaml and creating simplifiedTest.Content object
            File contentFile = new File("src/test/resources/microfocus/adm/performancecenter/plugins/common/rest/".concat("CreateTestFromContent.yaml"));
            com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent content =
                    mapper.readValue(contentFile, com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent.class);
            System.out.println(ReflectionToStringBuilder.toString(content, MULTI_LINE_STYLE));

            //reading CreateTestFromSimplifiedTest.yaml and creating SimplifiedTest object
            File simplifiedTestFile = new File("src/test/resources/microfocus/adm/performancecenter/plugins/common/rest/".concat("CreateTestFromSimplifiedTest.yaml"));
            SimplifiedTest simplifiedTest =
                    mapper.readValue(simplifiedTestFile, SimplifiedTest.class);
            System.out.println(ReflectionToStringBuilder.toString(simplifiedTest, MULTI_LINE_STYLE));

            //reading string and creating simplifiedTest.Content object
            String contentString = fileToStringWithoutTrim("CreateTestFromContent.yaml");
            com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent content2 =
                    mapper.readValue(contentString, com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent.class);
            System.out.println(ReflectionToStringBuilder.toString(content2, MULTI_LINE_STYLE));


            //reading CreateTestFromSimplifiedTest.yaml and creating SimplifiedTest object
            String simplifiedTestString = fileToStringWithoutTrim("CreateTestFromSimplifiedTest.yaml");
            SimplifiedTest simplifiedTest2 =
                    mapper.readValue(simplifiedTestString, SimplifiedTest.class);
            System.out.println(ReflectionToStringBuilder.toString(simplifiedTest2, MULTI_LINE_STYLE));

            com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test  test =
                    pcRestProxy.createOrUpdateTestFromYamlContent("testnamekuku", "Subject\\pathkuku", contentString);

            System.out.println(String.format("the following test was created/updated successfully: id: %s, testname: %s, path: %s", test.getID(), test.getName(), test.getTestFolderPath()));

            pcRestProxy.deleteTest(stringToInteger(test.getID()));

            System.out.println(String.format("test ID %s deleted successfully",test.getID()));

            com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.Test  test2 =
                    pcRestProxy.createOrUpdateTestFromYamlTest(simplifiedTestString);

            System.out.println(String.format("the following test was created/updated successfully: id: %s, testname: %s, path: %s", test2.getID(), test2.getName(), test2.getTestFolderPath()));

            pcRestProxy.deleteTest(stringToInteger(test2.getID()));

            System.out.println(String.format("test ID %s deleted successfully",test2.getID()));

        } catch (Exception e) {
            System.out.println("verifyTestCreationOrUpdateViaXmlOrYaml failed. Exception = " + e.getMessage());
            throw e;
        } finally {
            //testLogout();
        }
    }


    //endregion

    //region others

    private void testLogin() throws PcException, IOException {
        System.out.println("testLogin: starts");
        try {
            System.out.println("testLogin: Testing Login to PC server");
            Assert.assertTrue("testLogin: Authenticate success", pcRestProxy.authenticate(PcRestProxyBase.ALM_USER_NAME, PcRestProxyBase.ALM_PASSWORD));
        }
        catch (PcException ex) {
            System.out.println("testLogin: Failed to Authenticate. PcException = " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("testLogin: Failed to Authenticate. IOException = " + ex.getMessage());
        }
        System.out.println("testLogin: ends");
    }



    public PcTestInstances getTestInstancesByTestId(int testID) {
        System.out.println("getTestInstancesByTestId: starts");
        try {
            PcTestInstances pcTestInstances = pcRestProxy.getTestInstancesByTestId(testID);
            System.out.println(String.format("getTestInstancesByTestId: pcTestInstances are: %s", pcTestInstances.getTestInstancesList().toString()));
            return pcTestInstances;
        } catch (PcException|IOException ex) {
            System.out.println(String.format("getTestInstancesByTestId: failed getting TestInstances for test %s. PcException = %s", testID, ex.getMessage()));
        }
        System.out.println("testGetAllTestSets: ends");
        return  null;
    }


    private void testGetAllTestSets () throws PcException, IOException {
        System.out.println("testGetAllTestSets: starts");
        try {
            PcTestSets pcTestSets = pcRestProxy.GetAllTestSets();
            Assert.assertTrue("testGetAllTestSets: testsets received", !pcTestSets.getPcTestSetsList().isEmpty());
        }
        catch (PcException ex) {
            System.out.println("testGetAllTestSets: failed getting testsets. PcException = " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("testGetAllTestSets: failed getting testsets. IOException = " + ex.getMessage());
        }
        System.out.println("testGetAllTestSets: ends");
    }

    private int testUploadScript() throws PcException, IOException {
        System.out.println("testUploadScript: starts");
        try {
            System.out.println("testUploadScript: Uploading script to project");
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("microfocus/adm/performancecenter/plugins/common/rest/kilimanjaro.zip").getFile());
            int scriptId = pcRestProxy.uploadScript(PcRestProxyBase.testFolderPath, PcRestProxyBase.Overwrite, PcRestProxyBase.RuntimeOnly, PcRestProxyBase.KeepCheckedOut, file.getPath());
            System.out.println("testUploadScript: Uploading script to project to scriptID " + scriptId);
            System.out.println("testUploadScript: ends");
            return scriptId;
        }
        catch (PcException ex) {
            System.out.println("testUploadScript: Uploading script failed. PcException = " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("testUploadScript: Uploading script failed. IOException = " + ex.getMessage());
        }
        System.out.println("testUploadScript: ends");
        return 0;
    }

    private boolean getScripts() throws PcException, IOException {
        System.out.println("getScripts: starts");
        try {
            System.out.println("getScripts: getting all scripts from the project");
            PcScripts pcScripts = pcRestProxy.getScripts();
            String scriptIdsList = "ID of scripts in the project: ";
            for (PcScript pcScript: pcScripts.getPcScriptList()
                 ) {
                scriptIdsList = scriptIdsList.concat(String.format("%d, ", pcScript.getID()));
//                System.out.println(String.format("ID = %s, Name = %s, CreatedBy = %s, TestFolderPath = %s, WorkingMode = %s, Protocol = %s",
//                        Integer.toString(pcScript.getId()), pcScript.getParentId(), pcScript.getParentId(),
//                        pcScript.getFullPath(), pcScript.getWorkingMode(), pcScript.getProtocol()));

            }
            scriptIdsList = scriptIdsList.substring(0, scriptIdsList.length() - 2);
            System.out.println(scriptIdsList);
            return true;
        }
        catch (PcException ex) {
            System.out.println("getScripts: getting all scripts failed. PcException = " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("getScripts: getting all scripts failed. IOException = " + ex.getMessage());
        }
        System.out.println("getScripts: ends");
        return false;
    }

    private PcScript getScript (int Id) throws PcException, IOException {
        System.out.println("getScript: starts");
        try {
            System.out.println(String.format("getScript: getting script ID = %s from the project", Id));
            PcScript pcScript = pcRestProxy.getScript(Id);
            System.out.println(String.format("ID = %s, Name = %s, CreatedBy = %s, TestFolderPath = %s, WorkingMode = %s, Protocol = %s,",
                    Integer.toString(pcScript.getID()), pcScript.getName(), pcScript.getCreatedBy(),
                    pcScript.getTestFolderPath(), pcScript.getWorkingMode(), pcScript.getProtocol()));
            return pcScript;
        }
        catch (PcException ex) {
            System.out.println("getScript: getting script failed. PcException = " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("getScript: getting script failed. IOException = " + ex.getMessage());
        }
        System.out.println("getScript: ends");
        return null;
    }

    private PcTestPlanFolders getTestPlanFolders () throws PcException, IOException {
        System.out.println("getTestPlan: starts");
        try {
            System.out.println(String.format("getTestPlan"));
            PcTestPlanFolders pcTestPlanFolders = pcRestProxy.getTestPlanFolders();
            for (PcTestPlanFolder pcTestPlanFolder : pcTestPlanFolders.getPcTestPlanFolderList()
                 ) {
                System.out.println(String.format("Id = %s, Name = %s, ParentId = %s, FullPath = %s", pcTestPlanFolder.getId(), pcTestPlanFolder.getName(), pcTestPlanFolder.getParentId(), pcTestPlanFolder.getFullPath() ));
            }

            return pcTestPlanFolders;
        }
        catch (PcException|IOException ex) {
            System.out.println("getTestPlan: getting TestPlanFolders failed. PcException = " + ex.getMessage());
        }

        System.out.println("getTestPlan: ends");
        return null;
    }


    private PcTestPlanFolder CreateTestPlanFolder (String path, String name) throws PcException, IOException {
        System.out.println("CreateTestPlanFolder: starts");
        try {

            System.out.println(String.format("CreateTestPlanFolder: requesting to create Folder '%s' under folder path '%s'", path, name));
            PcTestPlanFolder pcTestPlanFolder = pcRestProxy.createTestPlanFolder(path, name);
            System.out.println(String.format("Folder created: Id = %s, Name = %s, ParentId = %s, FullPath = %s", pcTestPlanFolder.getId(), pcTestPlanFolder.getName(), pcTestPlanFolder.getParentId(), pcTestPlanFolder.getFullPath() ));


            return pcTestPlanFolder;
        }
        catch (PcException|IOException ex) {
            System.out.println("CreateTestPlanFolder: creating TestPlan Folder failed. PcException = " + ex.getMessage());
        }

        System.out.println("CreateTestPlanFolder: ends");
        return null;
    }

    private boolean verifyTestPlanFolderExist(String path) {
        System.out.println("verifyTestPlanFolderExist: starts");
        try {

            System.out.println(String.format("verifyTestPlanFolderExist: Verifying if Folder '%s' exists", path));
            boolean testPlanfolderExist = pcRestProxy.verifyTestPlanFolderExist(path);
            System.out.println(String.format(testPlanfolderExist ? "verifyTestPlanFolderExist: Folder '%s' does exist" : "verifyTestPlanFolderExist: Folder '%s' does not exist", path));

            return testPlanfolderExist;
        }
        catch (PcException|IOException ex) {
            System.out.println("verifyTestPlanFolderExist: creating TestPlan Folder failed. PcException = " + ex.getMessage());
        }

        System.out.println("verifyTestPlanFolderExist: ends");
        return false;
    }

    private ArrayList<PcTestPlanFolder> createTestPlanFolders(String[] paths) {
        System.out.println("verifyTestPlanFolderExist: starts");
        ArrayList<PcTestPlanFolder> pcTestPlanFolders = null;
        try {
        pcTestPlanFolders = pcRestProxy.createTestPlanFolders(paths);
        for (PcTestPlanFolder pcTestPlanFolder: pcTestPlanFolders
                ) {
            System.out.println(String.format("Id = %s, Name = %s, ParentId = %s, FullPath = %s", pcTestPlanFolder.getId(), pcTestPlanFolder.getName(), pcTestPlanFolder.getParentId(), pcTestPlanFolder.getFullPath() ));
        }
        }
        catch (PcException|IOException ex) {
            System.out.println("verifyTestPlanFolderExist: creating TestPlan Folder failed. PcException = " + ex.getMessage());
        }
        System.out.println("verifyTestPlanFolderExist: ends");
        return pcTestPlanFolders;
    }


    private PcScript getScript (String testFolderPath, String scriptName) throws PcException, IOException {
        System.out.println("getScript: starts");
        try {
            System.out.println(String.format("getScript: getting script folder = %s from the project", testFolderPath));
            PcScript pcScript = pcRestProxy.getScript(testFolderPath, scriptName);
            System.out.println(String.format("ID = %s, Name = %s, CreatedBy = %s, TestFolderPath = %s, WorkingMode = %s, Protocol = %s,",
                    Integer.toString(pcScript.getID()), pcScript.getName(), pcScript.getCreatedBy(),
                    pcScript.getTestFolderPath(), pcScript.getWorkingMode(), pcScript.getProtocol()));
            return pcScript;
        }
        catch (PcException ex) {
            System.out.println("getScript: getting script failed. PcException = " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("getScript: getting script failed. IOException = " + ex.getMessage());
        }
        System.out.println("getScript: ends");
        return null;
    }

    private boolean deleteScript(int scriptId) throws PcException, IOException {
        System.out.println("deleteScript: starts");
        try {
            System.out.println("deleteScript: deleting script from project");
            boolean deleteSuccess = pcRestProxy.deleteScript(scriptId);
            System.out.println(String.format("deleteScript: deleting script %s from project to  ", scriptId));
            System.out.println("deleteScript: ends");
            return deleteSuccess;
        }
        catch (PcException ex) {
            System.out.println("deleteScript: deleting script failed. PcException = " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("deleteScript: deleting script failed. IOException = " + ex.getMessage());
        }
        System.out.println("deleteScript: ends");
        return false;
    }

    private void testLogout() throws PcException, IOException {
        System.out.println("testLogout: starts");
        try {
        System.out.println("testLogout: Testing Logout from PC server");
        Assert.assertTrue("testLogout: Failed to logout with pcClient", pcRestProxy.logout());
        }
        catch (PcException ex) {
            System.out.println("testLogout: logout failed. PcException = " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("testLogout: logout failed. IOException = " + ex.getMessage());
        }
        System.out.println("testLogout: ends");
    }
    //endregion

}