/**
 Copyright 2018 Micro Focus International plc

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.microfocus.adm.performancecenter.plugins.common.pcentities;

/**
 * Created by bemh on 6/5/2017.
 * Partial implementation of the test xml structure
 */
public class PcTest {


    private int testId;
    private String testName;

    private int trendReportId = -1;



    public int getTestId() {
        return testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTrendReportId(int trendReportId) {
        this.trendReportId = trendReportId;
    }

    public int getTrendReportId() {
        return trendReportId;
    }

    public void setTestId(int testId){this.testId = testId;}

    public void setTestName(String testName){this.testName = testName;}
}
