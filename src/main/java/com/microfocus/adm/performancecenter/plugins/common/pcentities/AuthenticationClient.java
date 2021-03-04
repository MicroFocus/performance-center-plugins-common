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
import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;

public class AuthenticationClient {

    public String ClientIdKey;
    public String ClientSecretKey;

    public AuthenticationClient(String clientIdKey, String clientSecretKey) {
        ClientIdKey = clientIdKey;
        ClientSecretKey = clientSecretKey;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream.alias("AuthenticationClient", AuthenticationClient.class);
        return xstream.toXML(this);
    }

    public static AuthenticationClient xmlToObject(String xml){
        XStream xstream = new XStream();
        xstream.alias("AuthenticationClient", AuthenticationClient.class);
        return (AuthenticationClient)xstream.fromXML(xml);
    }
}