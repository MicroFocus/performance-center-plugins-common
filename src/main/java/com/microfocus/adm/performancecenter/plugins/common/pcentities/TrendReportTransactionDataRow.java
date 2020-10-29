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

/**
 * Holds Trending Transactions data
 */
public class TrendReportTransactionDataRow {

//<PCT_TYPE>TRT</PCT_TYPE>
//<PCT_NAME>Action_Transaction</PCT_NAME>
//<PCT_MINIMUM>0</PCT_MINIMUM>
//<PCT_MAXIMUM>0.001</PCT_MAXIMUM>
//<PCT_AVERAGE>0.001</PCT_AVERAGE>
//<PCT_MEDIAN>0.001</PCT_MEDIAN>
//<PCT_STDDEVIATION>0</PCT_STDDEVIATION>
//<PCT_COUNT1>40</PCT_COUNT1>
//<PCT_SUM1>0.027</PCT_SUM1>
//<PCT_PERCENTILE_25>0.001118</PCT_PERCENTILE_25>
//<PCT_PERCENTILE_75>0.001118</PCT_PERCENTILE_75>
//<PCT_PERCENTILE_90>0.001118</PCT_PERCENTILE_90>
//<PCT_PERCENTILE_91>0.0004311</PCT_PERCENTILE_91>
//<PCT_PERCENTILE_92>0.0004311</PCT_PERCENTILE_92>
//<PCT_PERCENTILE_93>0.0004311</PCT_PERCENTILE_93>
//<PCT_PERCENTILE_94>0.0004311</PCT_PERCENTILE_94>
//<PCT_PERCENTILE_95>0.0004311</PCT_PERCENTILE_95>
//<PCT_PERCENTILE_96>0.0004311</PCT_PERCENTILE_96>
//<PCT_PERCENTILE_97>0.0004311</PCT_PERCENTILE_97>
//<PCT_PERCENTILE_98>0.0004311</PCT_PERCENTILE_98>
//<PCT_PERCENTILE_99>0.0004311</PCT_PERCENTILE_99>

    private String PCT_TYPE;
    private String PCT_NAME;
    private String PCT_MINIMUM;
    private String PCT_MAXIMUM;
    private String PCT_AVERAGE;
    private String PCT_MEDIAN;
    private String PCT_STDDEVIATION;
    private String PCT_COUNT1;
    private String PCT_SUM1;
    private String PCT_PERCENTILE_25;
    private String PCT_PERCENTILE_75;
    private String PCT_PERCENTILE_90;
    private String PCT_PERCENTILE_91;
    private String PCT_PERCENTILE_92;
    private String PCT_PERCENTILE_93;
    private String PCT_PERCENTILE_94;
    private String PCT_PERCENTILE_95;
    private String PCT_PERCENTILE_96;
    private String PCT_PERCENTILE_97;
    private String PCT_PERCENTILE_98;
    private String PCT_PERCENTILE_99;

    public static TrendReportTransactionDataRow xmlToObject(String xml)
    {
        XStream xstream = new XStream();
        xstream.alias("TransactionsDataRow" , TrendReportTransactionDataRow.class);
        return (TrendReportTransactionDataRow)xstream.fromXML(xml);
    }


    public String getPCT_TYPE() {
        return PCT_TYPE;
    }

    public String getPCT_NAME() {
        return PCT_NAME;
    }

    public String getPCT_MINIMUM() {
        return PCT_MINIMUM;
    }

    public String getPCT_MAXIMUM() {
        return PCT_MAXIMUM;
    }

    public String getPCT_AVERAGE() {
        return PCT_AVERAGE;
    }

    public String getPCT_MEDIAN() {
        return PCT_MEDIAN;
    }

    public String getPCT_STDDEVIATION() {
        return PCT_STDDEVIATION;
    }

    public String getPCT_COUNT1() {
        return PCT_COUNT1;
    }

    public String getPCT_SUM1() {
        return PCT_SUM1;
    }

    public String getPCT_PERCENTILE_25() {
        return PCT_PERCENTILE_25;
    }

    public String getPCT_PERCENTILE_75() {
        return PCT_PERCENTILE_75;
    }

    public String getPCT_PERCENTILE_90() {
        return PCT_PERCENTILE_90;
    }

    public String getPCT_PERCENTILE_91() {
        return PCT_PERCENTILE_91;
    }

    public String getPCT_PERCENTILE_92() {
        return PCT_PERCENTILE_92;
    }

    public String getPCT_PERCENTILE_93() {
        return PCT_PERCENTILE_93;
    }

    public String getPCT_PERCENTILE_94() {
        return PCT_PERCENTILE_94;
    }

    public String getPCT_PERCENTILE_95() {
        return PCT_PERCENTILE_95;
    }

    public String getPCT_PERCENTILE_96() {
        return PCT_PERCENTILE_96;
    }

    public String getPCT_PERCENTILE_97() {
        return PCT_PERCENTILE_97;
    }

    public String getPCT_PERCENTILE_98() {
        return PCT_PERCENTILE_98;
    }

    public String getPCT_PERCENTILE_99() {
        return PCT_PERCENTILE_99;
    }
}
