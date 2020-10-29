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
 * Created by bemh on 6/1/2017.
 */
public class PcTestSet {

    String TestSetName;
    String TestSetComment;
    int TestSetParentId;
    int TestSetID;

    public static PcTestSet xmlToObject(String xml){
        XStream xstream = new XStream();
        xstream.alias("TestSetID" , PcRunResult.class);
        return (PcTestSet)xstream.fromXML(xml);
    }


    public String getTestSetName(){
        return TestSetName;
    }
    public String getTestSetComment(){
        return TestSetComment;
    }

    public int getTestSetParentId(){
        return TestSetParentId;
    }

    public int getTestSetID(){
        return TestSetID;
    }

}
