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
package com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.monitorsofw;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.common.Common;
import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MonitorOFW")
public class MonitorOFW {

    @XmlElement
    private String ID;

    public MonitorOFW() {
    }

    public MonitorOFW(int ID) {
        setID(ID);
    }

    public static MonitorOFW xmlToObject(String xml) {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("MonitorOFW", MonitorOFW.class);
        xstream.setClassLoader(MonitorOFW.class.getClassLoader());
        xstream.setMode(XStream.NO_REFERENCES);
        return (MonitorOFW) xstream.fromXML(xml);
    }

    @Override
    public String toString() {
        return "MonitorOFW{" + "ID = " + ID;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("MonitorOFW", MonitorOFW.class);
        xstream.aliasField("ID", MonitorOFW.class, "ID");
        xstream.aliasField("MonitorOFW", MonitorOFW.class, "MonitorOFW");
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream.toXML(this);
    }

    public String getID() {
        return ID;
    }

    public void setID(int value) {
        this.ID = Common.integerToString(value);
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
