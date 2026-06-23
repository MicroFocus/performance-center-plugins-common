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

import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;
import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;

public class PcTestSetFolder {
    private String xmlns = PcRestProxy.PC_API_XMLNS;
    private int TestSetFolderId;
    private int Parent;
    private String TestSetFolderName;

    public static PcTestSetFolder xmlToObject(String xml) {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("TestSetFolder", PcTestSetFolder.class);
        xstream.useAttributeFor(PcTestSetFolder.class, "xmlns");
        xstream.setClassLoader(PcTestSetFolder.class.getClassLoader());
        return (PcTestSetFolder) xstream.fromXML(xml);
    }

    public int getTestSetFolderId() {
        return TestSetFolderId;
    }

    public void setTestSetFolderId(int testSetFolderId) {
        TestSetFolderId = testSetFolderId;
    }

    public int getParent() {
        return Parent;
    }

    public void setParent(int parent) {
        Parent = parent;
    }

    public String getTestSetFolderName() {
        return TestSetFolderName;
    }

    public void setTestSetFolderName(String testSetFolderName) {
        TestSetFolderName = testSetFolderName;
    }
}
