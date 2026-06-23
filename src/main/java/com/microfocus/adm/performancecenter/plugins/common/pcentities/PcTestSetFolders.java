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

public class PcTestSetFolders {
    private ArrayList<PcTestSetFolder> pcTestSetFolderList;

    public PcTestSetFolders() {
        pcTestSetFolderList = new ArrayList<PcTestSetFolder>();
    }

    public static PcTestSetFolders xmlToObject(String xml) {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("TestSetFolder", PcTestSetFolder.class);
        xstream.alias("TestSetFolders", PcTestSetFolders.class);
        xstream.addImplicitCollection(PcTestSetFolders.class, "pcTestSetFolderList");
        xstream.setClassLoader(PcTestSetFolders.class.getClassLoader());
        return (PcTestSetFolders) xstream.fromXML(xml);
    }

    public ArrayList<PcTestSetFolder> getPcTestSetFolderList() {
        return pcTestSetFolderList;
    }

    public void setPcTestSetFolderList(ArrayList<PcTestSetFolder> pcTestSetFolderList) {
        this.pcTestSetFolderList = pcTestSetFolderList;
    }
}
