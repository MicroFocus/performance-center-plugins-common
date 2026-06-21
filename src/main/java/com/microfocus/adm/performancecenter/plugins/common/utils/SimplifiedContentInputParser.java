package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.SimplifiedTest;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent;
import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;

import java.io.IOException;

final class SimplifiedContentInputParser {

    ParseResult parse(String inputTestName, String inputTestFolderPath, String testOrContent) throws IOException {
        SimplifiedContent simplifiedContent;
        String testName = inputTestName;
        String testFolderPath = inputTestFolderPath;

        if (testName.isEmpty() && testFolderPath.isEmpty()) {
            SimplifiedTest simplifiedTest = PcRestProxy.xmlOrYamlStringToSimplifiedTest(testOrContent);
            simplifiedContent = simplifiedTest.getTest_content();
            testName = simplifiedTest.getTest_name();
            testFolderPath = simplifiedTest.getTest_folder_path();
        } else {
            simplifiedContent = PcRestProxy.xmlOrYamlStringToSimplifiedContent(testOrContent);
        }

        return new ParseResult(testName, testFolderPath, verifyTestFolderPath(testFolderPath), simplifiedContent);
    }

    private String verifyTestFolderPath(String testFolderPath) {
        String testFolderPathWithSubject = "Subject\\".concat(testFolderPath);
        return testFolderPathWithSubject.replace("/", "\\");
    }

    static final class ParseResult {
        private final String testName;
        private final String testFolderPath;
        private final String testFolderPathWithSubject;
        private final SimplifiedContent simplifiedContent;

        ParseResult(String testName, String testFolderPath, String testFolderPathWithSubject, SimplifiedContent simplifiedContent) {
            this.testName = testName;
            this.testFolderPath = testFolderPath;
            this.testFolderPathWithSubject = testFolderPathWithSubject;
            this.simplifiedContent = simplifiedContent;
        }

        String getTestName() {
            return testName;
        }

        String getTestFolderPathWithSubject() {
            return testFolderPathWithSubject;
        }

        String getTestFolderPath() {
            return testFolderPath;
        }

        SimplifiedContent getSimplifiedContent() {
            return simplifiedContent;
        }
    }
}

