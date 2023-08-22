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
package com.microfocus.adm.performancecenter.plugins.common.pcentities;

import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;

public class PcTestSets {
    private ArrayList<PcTestSet> pcTestSetsList;

    public PcTestSets() {
        pcTestSetsList = new ArrayList<PcTestSet>();
    }

    public static PcTestSets xmlToObject(String xml) {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("TestSet", PcTestSet.class);
        xstream.alias("TestSets", PcTestSets.class);
        xstream.addImplicitCollection(PcTestSets.class, "pcTestSetsList");
        xstream.setClassLoader(PcTestSets.class.getClassLoader());
        return (PcTestSets) xstream.fromXML(xml);
    }

    public ArrayList<PcTestSet> getPcTestSetsList() {
        return pcTestSetsList;
    }

    public void setPcTestSetsList(ArrayList<PcTestSet> pcTestSetsList) {
        this.pcTestSetsList = pcTestSetsList;
    }
}
