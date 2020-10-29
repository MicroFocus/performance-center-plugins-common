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

/**
 * Holds Trending Regular
 */
public class TrendReportRegularDataRows {

    private ArrayList<TrendReportRegularDataRow> trendReportRegularDataRowList;

    public TrendReportRegularDataRows(){ trendReportRegularDataRowList = new ArrayList<TrendReportRegularDataRow>();}

    public static TrendReportRegularDataRows xmlToObject(String xml)
    {
        XStream xstream = new XStream();
        xstream.alias("RegularData" , TrendReportRegularDataRows.class);
        xstream.alias("RegularDataRow" , TrendReportRegularDataRow.class);
        xstream.addImplicitCollection(TrendReportRegularDataRows.class, "trendReportRegularDataRowList");
        xstream.setClassLoader(TrendReportRegularDataRows.class.getClassLoader());
        return (TrendReportRegularDataRows)xstream.fromXML(xml);
    }

    public ArrayList<TrendReportRegularDataRow> getTrendReportRegularDataRowList() {
        return trendReportRegularDataRowList;
    }

}


