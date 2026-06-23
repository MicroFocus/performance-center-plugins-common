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
public class TestSetCreateRequest {
    private String xmlns = PcRestProxy.PC_API_XMLNS;
    private String TestSetName;
    private String TestSetComment;
    private int TestSetParentId;

    public TestSetCreateRequest() {
    }

    public TestSetCreateRequest(String testSetName, int testSetParentId, String testSetComment) {
        this.TestSetName = testSetName;
        this.TestSetParentId = testSetParentId;
        this.TestSetComment = testSetComment;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.useAttributeFor(TestSetCreateRequest.class, "xmlns");
        xstream.alias("TestSet", TestSetCreateRequest.class);
        xstream.aliasField("TestSetName", TestSetCreateRequest.class, "TestSetName");
        xstream.aliasField("TestSetComment", TestSetCreateRequest.class, "TestSetComment");
        xstream.aliasField("TestSetParentId", TestSetCreateRequest.class, "TestSetParentId");
        return xstream.toXML(this);
    }

    public PcTestSet getPcTestSetFromResponse(String xml) {
        return PcTestSet.xmlToObject(xml);
    }

    public String getTestSetName() {
        return TestSetName;
    }

    public void setTestSetName(String testSetName) {
        TestSetName = testSetName;
    }

    public String getTestSetComment() {
        return TestSetComment;
    }

    public void setTestSetComment(String testSetComment) {
        TestSetComment = testSetComment;
    }

    public int getTestSetParentId() {
        return TestSetParentId;
    }

    public void setTestSetParentId(int testSetParentId) {
        TestSetParentId = testSetParentId;
    }
}
