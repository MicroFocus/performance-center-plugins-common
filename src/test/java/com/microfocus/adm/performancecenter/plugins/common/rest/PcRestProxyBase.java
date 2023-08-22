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

import com.microfocus.adm.performancecenter.plugins.common.pcentities.PostRunAction;

import java.io.PrintStream;

public interface PcRestProxyBase {

    public static final String SERVER_AND_PORT = "jenkins.server:8082";
    public static final String LRE_SERVER_NAME_WITH_TENANT = "http://mylreserver/LRE/?tenant=fa128c06-5436-413d-9cfa-9f04bb738df3";
    public static final String ALM_USER_NAME = "sa";
    public static final String ALM_PASSWORD = "saqa";
    public static final String LRE_ID_KEY = "I_KEY_64e99e22-4fa7-436b-a629-5387a67aa66d";
    public static final String LRE_SECRET_KEY = "S_KEY_80f55a6f-01aa-4e2c-9262-af634b8aa2bc";
    public static final String ALM_DOMAIN = "DANIEL";
    public static final String ALM_PROJECT = "proj1";
    public static final String TEST_ID = "175";
    public static final int TEST_INSTANCE_ID = 8;
    public static final String TIMESLOT_DURATION_HOURS = "0";
    public static final String TIMESLOT_DURATION_MINUTES = "34";
    public static final String TIMESLOT_ID = "1097";
    public static final PostRunAction POST_RUN_ACTION = PostRunAction.COLLATE_AND_ANALYZE;
    public static final boolean VUDS_MODE = false;
    //public static final String DESCRIPTION = "Testing HPE Performance Center Jenkins plugin";
    public static final String RUN_ID = "13";
    public static final String RUN_ID_WAIT = "8";
    public static final String REPORT_ID = "1058";
    public static final String STOP_MODE = "stop";
    public static final String WEB_PROTOCOL = "http";
    public static final String WEB_PROTOCOL_62 = "http";
    public static final Boolean IS_HTTPS = false;
    public static final String TESTINSTANCEID = "MANUAL";
    public static final PrintStream LOGGER = null;
    public static final String RETRY = "NO_RETRY";
    public static final String RETRYDELAY = "5";
    public static final String RETRYOCCURRENCES = "3";
    public static final String pcReportArchiveName = "Reports.zip";
    public static final String testFolderPath = "Subject\\ts\\scripts3";
    public static final boolean Overwrite = true;
    public static final boolean RuntimeOnly = true;
    public static final boolean KeepCheckedOut = false;
    public static final String scriptPath = "C:\\temp\\kilimanjaro.zip";


    public static final String runResponseEntity = "<Run xmlns=\"http://www.hp.com/PC/REST/API\">" +
            "<TestID>" + TEST_ID + "</TestID>" +
            "<TestInstanceID>" + TEST_INSTANCE_ID + "</TestInstanceID>" +
            "<PostRunAction>" + POST_RUN_ACTION.getValue() + "</PostRunAction>" +
            "<TimeslotID>1076</TimeslotID>" +
            "<VudsMode>false</VudsMode>" +
            "<ID>" + RUN_ID + "</ID>" +
            "<Duration>" + TIMESLOT_DURATION_MINUTES + "</Duration>" +
            "<RunState>*</RunState>" +
            "<RunSLAStatus />" +
            "</Run>";

    public static final String emptyResultsEntity = "<RunResults xmlns=\"http://www.hp.com/PC/REST/API\" />";

    public static final String runResultsEntity = "<RunResults xmlns=\"http://www.hp.com/PC/REST/API\">" +
            "<RunResult>" +
            "<ID>1302</ID>" +
            "<Name>output.mdb.zip</Name>" +
            "<Type>Output Log</Type>" +
            "<RunID>" + RUN_ID + "</RunID>" +
            "</RunResult>" +
            "<RunResult>" +
            "<ID>1303</ID>" +
            "<Name>RawResults.zip</Name>" +
            "<Type>Raw Results</Type>" +
            "<RunID>" + RUN_ID + "</RunID>" +
            "</RunResult>" +
            "<RunResult>" +
            "<ID>1304</ID>" +
            "<Name>Results.zip</Name>" +
            "<Type>Analyzed Result</Type>" +
            "<RunID>" + RUN_ID + "</RunID>" +
            "</RunResult>" +
            "<RunResult>" +
            "<ID>" + REPORT_ID + "</ID>" +
            "<Name>Reports.zip</Name>" +
            "<Type>HTML Report</Type>" +
            "<RunID>" + RUN_ID + "</RunID>" +
            "</RunResult>" +
            "<RunResult>" +
            "<ID>1306</ID>" +
            "<Name>HighLevelReport_7.xls</Name>" +
            "<Type>Rich Report</Type>" +
            "<RunID>" + RUN_ID + "</RunID>" +
            "</RunResult>" +
            "</RunResults>";

    public static final String pcAuthenticationFailureMessage = "Exception of type 'HPE.PC.API.Model.Exceptions.InvalidAuthenticationDataException' was thrown. Error code: 1100";

    public static final String pcNoTimeslotExceptionMessage = "Failed to retrieve reservation information for reservation " + TIMESLOT_ID + ". Error code: 1202";

    public static final String pcStopNonExistRunFailureMessage = "Failed to retrieve run " + RUN_ID + " information from domain " + ALM_DOMAIN + ", project " + ALM_PROJECT + ". Error code: 1300";

    public static final String testResponseEntity = "<Test>" +
            "<ID>2</ID>" +
            "<Name>test1</Name>" +
            "</Test>";


}
