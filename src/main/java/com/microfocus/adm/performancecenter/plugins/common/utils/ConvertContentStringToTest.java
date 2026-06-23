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

package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcException;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.Content;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.analysistemplate.AnalysisTemplate;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.automatictrending.AutomaticTrending;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.diagnostics.Diagnostics;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.elasticcontrollerconfiguration.ElasticControllerConfiguration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.elasticloadgeneratorconfiguration.ElasticLoadGeneratorConfiguration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.globalcommandline.GlobalCommandLine;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.globalrts.GlobalRTS;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.Group;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.lgdistribution.LGDistribution;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.monitorprofiles.MonitorProfile;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.monitorsofw.MonitorOFW;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.Scheduler;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.sla.SLA;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.workloadtype.WorkloadType;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent;
import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;

import java.io.IOException;
import java.util.ArrayList;

public class ConvertContentStringToTest {
    private final PcRestProxy pcRestProxy;
    private String testName;
    private String testFolderPath;
    private String testFolderPathWithSubject;
    private String testOrContent;
    private Content content;

    public ConvertContentStringToTest(PcRestProxy pcRestProxy) {
        this.pcRestProxy = pcRestProxy;
    }

    public ConvertContentStringToTest(PcRestProxy pcRestProxy, String testName, String testFolderPath, String testOrContent) {
        this(pcRestProxy);
        this.testName = testName;
        this.testFolderPath = testFolderPath;
        this.testOrContent = testOrContent;
    }

    public static class ConversionResult {
        private final String testName;
        private final String testFolderPath;
        private final String testFolderPathWithSubject;
        private final Content content;

        public ConversionResult(String testName, String testFolderPath, String testFolderPathWithSubject, Content content) {
            this.testName = testName;
            this.testFolderPath = testFolderPath;
            this.testFolderPathWithSubject = testFolderPathWithSubject;
            this.content = content;
        }

        public String getTestName() {
            return testName;
        }

        public String getTestFolderPath() {
            return testFolderPath;
        }

        public String getTestFolderPathWithSubject() {
            return testFolderPathWithSubject;
        }

        public Content getContent() {
            return content;
        }
    }

    public String getTestName() {
        return testName;
    }

    public String getTestFolderPath() {
        return testFolderPath;
    }

    public String getTestFolderPathWithSubject() {
        return testFolderPathWithSubject;
    }

    public Content getContent() {
        return content;
    }

    public ConversionResult convert(String testName, String testFolderPath, String testOrContent) throws IOException, PcException {
        SimplifiedContentInputParser.ParseResult parseResult = new SimplifiedContentInputParser().parse(testName, testFolderPath, testOrContent);
        SimplifiedContent simplifiedContent = new SimplifiedGroupScriptResolver(pcRestProxy).resolve(parseResult.getSimplifiedContent());

        ContentPartsFactory contentPartsFactory = new ContentPartsFactory();
        GroupFactory groupFactory = new GroupFactory();

        String controller = contentPartsFactory.getController(simplifiedContent);
        WorkloadType workloadType = contentPartsFactory.getWorkloadType();
        LGDistribution lgDistribution = contentPartsFactory.getLgDistribution(simplifiedContent);
        Scheduler scheduler = new SchedulerFactory().build(simplifiedContent);
        AutomaticTrending automaticTrending = contentPartsFactory.getAutomaticTrending(simplifiedContent);
        ElasticLoadGeneratorConfiguration elasticLoadGeneratorConfiguration =
                contentPartsFactory.getElasticLoadGeneratorConfiguration(simplifiedContent);
        ElasticControllerConfiguration elasticControllerConfiguration =
                contentPartsFactory.getElasticControllerConfiguration(simplifiedContent);
        GlobalCommandLine globalCommandLine = groupFactory.buildGlobalCommandLine(simplifiedContent.getGroup());
        ArrayList<Group> groups = groupFactory.createGroups(simplifiedContent.getGroup(), lgDistribution);

        ArrayList<MonitorProfile> monitorProfiles = null;
        AnalysisTemplate analysisTemplate = null;
        ArrayList<MonitorOFW> monitorsOFW = null;
        SLA sla = null;
        Diagnostics diagnostics = null;
        GlobalRTS globalRTS = null;

        Content convertedContent = new Content(controller, workloadType, lgDistribution, monitorProfiles, groups, scheduler,
                analysisTemplate, automaticTrending, monitorsOFW, sla, diagnostics, globalCommandLine,
                globalRTS, elasticLoadGeneratorConfiguration, elasticControllerConfiguration);

        return new ConversionResult(
                parseResult.getTestName(),
                parseResult.getTestFolderPath(),
                parseResult.getTestFolderPathWithSubject(),
                convertedContent
        );
    }

    @Deprecated
    public ConvertContentStringToTest invoke() throws IOException, PcException {
        ConversionResult result = convert(testName, testFolderPath, testOrContent);
        this.testName = result.getTestName();
        this.testFolderPath = result.getTestFolderPath();
        this.testFolderPathWithSubject = result.getTestFolderPathWithSubject();
        this.content = result.getContent();
        return this;
    }
}
