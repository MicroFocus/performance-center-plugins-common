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

import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;
import com.thoughtworks.xstream.XStream;

/**
 *
 */
public class TrendReportRequest {

    @SuppressWarnings("unused")
    private String xmlns = PcRestProxy.PC_API_XMLNS;

    private String project;
    private int runId;
    private TrendedRange trandedRange;

    public TrendReportRequest(String project, int runId, TrendedRange trandedRange) {
        this.project = project;
        this.runId = runId;
        this.trandedRange = trandedRange;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream.useAttributeFor(TrendReportRequest.class, "xmlns");
        xstream.alias("TrendReport", TrendReportRequest.class);
        xstream.aliasField("Project", TrendReportRequest.class, "project");
        xstream.aliasField("RunId", TrendReportRequest.class, "runId");
        xstream.aliasField("TrendedRange", TrendReportRequest.class, "trendedRange");
        return xstream.toXML(this);
    }
}
