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

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds Trending Root data
 */
public  class TrendReportTransactionDataRoot {

    private List trendReportRoot;

    public TrendReportTransactionDataRoot(){
        trendReportRoot = new ArrayList();
    }

    public static TrendReportTransactionDataRoot xmlToObject(String xml)
    {
        XStream xstream = new XStream();
        xstream.alias("Root" , TrendReportTransactionDataRoot.class);
        xstream.alias("TransactionsData" , TrendReportTransactionDataRows.class);
        xstream.alias("TransactionsDataRow" , TrendReportTransactionDataRow.class);
        xstream.alias("MonitorsData" , TrendReportMonitorsDataRows.class);
        xstream.alias("MonitorsDataRow" , TrendReportMonitorsDataRow.class);
        xstream.alias("RegularData" , TrendReportRegularDataRows.class);
        xstream.alias("RegularDataRow" , TrendReportRegularDataRow.class);
        xstream.addImplicitCollection(TrendReportTransactionDataRoot.class, "trendReportRoot");
        xstream.addImplicitCollection(TrendReportTransactionDataRows.class, "trendReportTransactionDataRowList");
        xstream.addImplicitCollection(TrendReportMonitorsDataRows.class, "trendReportMonitorsDataRowList");
        xstream.addImplicitCollection(TrendReportRegularDataRows.class, "trendReportRegularDataRowList");

        xstream.setClassLoader(TrendReportTransactionDataRoot.class.getClassLoader());
        return (TrendReportTransactionDataRoot)xstream.fromXML(xml);
    }

    public List getTrendReportRoot() {
        return trendReportRoot;
    }


}
