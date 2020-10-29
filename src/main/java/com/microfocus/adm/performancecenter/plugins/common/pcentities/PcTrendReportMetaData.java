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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PcTrendReportMetaData {
    private static ArrayList<PcTrendedRun> Results;
    static Document dom;
    static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

//    public PcTrendReportMetaData() {
//        Results = new ArrayList<PcTrendedRun>();
//    }

    public static ArrayList<PcTrendedRun> xmlToObject(String xml)
    {
        Results = new ArrayList<PcTrendedRun>();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
            Element doc = dom.getDocumentElement();
            NodeList nListTrendedRun = doc.getElementsByTagName("TrendedRun");

            for (int i=0;i<nListTrendedRun.getLength();i++){
                Node nTrendedRun = nListTrendedRun.item(i);
                NodeList nListChild =  nTrendedRun.getChildNodes();
                PcTrendedRun pcTR = new PcTrendedRun();
                for (int j=0;j < nListChild.getLength();j++) {
                    if (nListChild.item(j).getNodeName().equals("RunID")){
                        pcTR.setRunID(Integer.parseInt(nListChild.item(j).getTextContent()));
                    }
                    if (nListChild.item(j).getNodeName().equals("State")){
                        pcTR.setState(nListChild.item(j).getTextContent());
                    }
                }
               Results.add(pcTR);
            }

        }catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return Results;
    }

}
