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
public class TestSetFolderCreateRequest {
    private String xmlns = PcRestProxy.PC_API_XMLNS;
    private String TestSetFolderName;
    private int Parent;

    public TestSetFolderCreateRequest() {
    }

    public TestSetFolderCreateRequest(String testSetFolderName, int parent) {
        this.TestSetFolderName = testSetFolderName;
        this.Parent = parent;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.useAttributeFor(TestSetFolderCreateRequest.class, "xmlns");
        xstream.alias("TestSetFolder", TestSetFolderCreateRequest.class);
        xstream.aliasField("TestSetFolderName", TestSetFolderCreateRequest.class, "TestSetFolderName");
        xstream.aliasField("Parent", TestSetFolderCreateRequest.class, "Parent");
        return xstream.toXML(this);
    }

    public PcTestSetFolder getPcTestSetFolderFromResponse(String xml) {
        return PcTestSetFolder.xmlToObject(xml);
    }

    public String getTestSetFolderName() {
        return TestSetFolderName;
    }

    public void setTestSetFolderName(String testSetFolderName) {
        TestSetFolderName = testSetFolderName;
    }

    public int getParent() {
        return Parent;
    }

    public void setParent(int parent) {
        Parent = parent;
    }
}
