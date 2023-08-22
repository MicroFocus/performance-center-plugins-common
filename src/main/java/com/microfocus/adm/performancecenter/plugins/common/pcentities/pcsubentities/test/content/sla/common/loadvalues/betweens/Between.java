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
package com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.sla.common.loadvalues.betweens;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.common.Common;
import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Between")
public class Between {
    @XmlElement
    private String From;
    @XmlElement
    private String To;

    public Between() {
    }

    public Between(int from, int to) {
        setFrom(from);
        setTo(to);
    }

    public static Between xmlToObject(String xml) {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("Between", Between.class);
        xstream.setClassLoader(Between.class.getClassLoader());
        xstream.setMode(XStream.NO_REFERENCES);
        return (Between) xstream.fromXML(xml);
    }

    @Override
    public String toString() {
        return "Between{" + "From = " + From +
                ", To = " + To + "}";
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("Between", Between.class);
        xstream.aliasField("From", Between.class, "From");
        xstream.aliasField("To", Between.class, "To");
        xstream.aliasField("Between", Between.class, "Between");
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream.toXML(this);
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(int value) {
        this.From = Common.integerToString(value);
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getTo() {
        return To;
    }

    public void setTo(int value) {
        this.To = Common.integerToString(value);
    }

    public void setTo(String to) {
        To = to;
    }
}