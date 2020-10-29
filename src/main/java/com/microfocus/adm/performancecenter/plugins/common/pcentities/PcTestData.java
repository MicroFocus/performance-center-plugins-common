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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by bemh on 6/5/2017.
 * Partial implementation of the test xml structure
 */
public class PcTestData {




    static Document dom;
    static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();


    public static PcTest xmlToObject(String xml){

        PcTest pcTest = new PcTest();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(new InputSource(new StringReader(xml)));
            Element doc = dom.getDocumentElement();
            NodeList nListTestNodes = doc.getElementsByTagName("AutomaticTrending");
            if (nListTestNodes.getLength() >0 )
            {
                NodeList nListChild = nListTestNodes.item(0).getChildNodes();
                for (int j=0;j < nListChild.getLength();j++) {
                    if (nListChild.item(j).getNodeName().equals("ReportId")){
                        pcTest.setTrendReportId(Integer.parseInt(nListChild.item(j).getTextContent()));
                        break;
                    }
                }
            }

            nListTestNodes = doc.getElementsByTagName("ID");
            pcTest.setTestId(Integer.parseInt(nListTestNodes.item(0).getFirstChild().getNodeValue()));

            nListTestNodes = doc.getElementsByTagName("Name");
            pcTest.setTestName(nListTestNodes.item(0).getFirstChild().getNodeValue());




        }catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return pcTest;
    }

}
