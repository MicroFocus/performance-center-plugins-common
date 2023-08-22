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

import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;

public class PcRunResponse extends PcRunRequest {
    private int ID;
    private int Duration;
    private String RunState;
    private String RunSLAStatus;

    public static PcRunResponse xmlToObject(String xml) {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.setClassLoader(PcRunResponse.class.getClassLoader());
        xstream.alias("Run", PcRunResponse.class);
        return (PcRunResponse) xstream.fromXML(xml);

    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    public String getRunState() {
        return RunState;
    }

    public void setRunState(String runState) {
        RunState = runState;
    }

    public String getRunSLAStatus() {
        return RunSLAStatus;
    }

    public void setRunSLAStatus(String runSLAStatus) {
        RunSLAStatus = runSLAStatus;
    }
}
