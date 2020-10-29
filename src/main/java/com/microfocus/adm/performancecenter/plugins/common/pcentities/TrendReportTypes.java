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
 * Holds Trending Types Enum
 */
public class TrendReportTypes {

    public enum DataType {
        Transaction, Monitors, Regular
    }

    public enum PctType {
        TRT, TPS, TRS, UDP, VU, WEB
    }

    public enum Measurement{
        getPCT_TYPE,
        getPCT_NAME,
        PCT_MINIMUM,
        PCT_MAXIMUM,
        PCT_AVERAGE,
        PCT_MEDIAN,
        PCT_STDDEVIATION,
        PCT_COUNT1,
        PCT_SUM1,
        PCT_MACHINE,
        PCT_PERCENTILE_25,
        PCT_PERCENTILE_75,
        PCT_PERCENTILE_90,
        PCT_PERCENTILE_91,
        PCT_PERCENTILE_92,
        PCT_PERCENTILE_93,
        PCT_PERCENTILE_94,
        PCT_PERCENTILE_95,
        PCT_PERCENTILE_96,
        PCT_PERCENTILE_97,
        PCT_PERCENTILE_98,
        PCT_PERCENTILE_99
    }
}
