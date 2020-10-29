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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by bemh on 5/23/2017.
 */
public class TestInstanceCreateRequest {

    private String xmlns = PcRestProxy.PC_API_XMLNS;

    private int testId;
    private int testSetId;

    public TestInstanceCreateRequest(int testId, int testSetId) {
        this.testId = testId;
        this.testSetId = testSetId;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream.useAttributeFor(TestInstanceCreateRequest.class, "xmlns");
        xstream.alias("TestInstance", TestInstanceCreateRequest.class);
        xstream.aliasField("TestID", TestInstanceCreateRequest.class, "testId");
        xstream.aliasField("TestSetID", TestInstanceCreateRequest.class, "testSetId");
        return xstream.toXML(this);
    }

    public int getTestInstanceIDFromResponse(String xml, String getTestInstanceID) throws IOException, SAXException, ParserConfigurationException {


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8")));
        Document doc = builder.parse(is);
        Element element = doc.getDocumentElement();


        NodeList nListTrendedRun = doc.getElementsByTagName("TestInstanceID");
        return Integer.parseInt(nListTrendedRun.item(0).getTextContent());

    }
}
