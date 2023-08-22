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
package com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.goalscheduler;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.goalscheduler.goalhitspersecond.GoalHitsPerSecond;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.goalscheduler.goaltransactionspersecond.GoalTransactionsPerSecond;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.goalscheduler.goalvirtualusers.GoalVirtualUsers;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.goalscheduler.scenariosettings.ScenarioSettings;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.GoalTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GoalScheduler")
public class GoalScheduler {

    @XmlElement
    private String GoalProfileName;

    /*
    Required. Goal types can be:
        VirtualUsers
        HitsPerSecond
        TransactionsPerSecond
    */
    @XmlElement
    private String GoalType;

    /*Mandatory when GoalType is HitsPerSecond*/
    @XmlElement
    private GoalHitsPerSecond GoalHitsPerSecond;

    /*Mandatory when GoalType is TransactionsPerSecond */
    @XmlElement
    private GoalTransactionsPerSecond GoalTransactionsPerSecond;

    @XmlElement
    private GoalVirtualUsers GoalVirtualUsers;

    @XmlElement
    private boolean DoNotChangeScriptThinkTime;

    @XmlElement
    private ScenarioSettings ScenarioSettings;


    public GoalScheduler() {
    }


    public GoalScheduler(String goalProfileName, String goalType, GoalHitsPerSecond goalHitsPerSecond, GoalTransactionsPerSecond goalTransactionsPerSecond, GoalVirtualUsers goalVirtualUsers, boolean doNotChangeScriptThinkTime, ScenarioSettings scenarioSettings) {
        setGoalProfileName(goalProfileName);
        setGoalType(goalType);
        setGoalHitsPerSecond(goalHitsPerSecond);
        setGoalTransactionsPerSecond(goalTransactionsPerSecond);
        setGoalVirtualUsers(goalVirtualUsers);
        setDoNotChangeScriptThinkTime(doNotChangeScriptThinkTime);
        setScenarioSettings(scenarioSettings);
    }


    public GoalScheduler(String goalProfileName, GoalTypeValues goalType, GoalHitsPerSecond goalHitsPerSecond, GoalTransactionsPerSecond goalTransactionsPerSecond, GoalVirtualUsers goalVirtualUsers, boolean doNotChangeScriptThinkTime, ScenarioSettings scenarioSettings) {
        setGoalProfileName(goalProfileName);
        setGoalType(goalType);
        setGoalHitsPerSecond(goalHitsPerSecond);
        setGoalTransactionsPerSecond(goalTransactionsPerSecond);
        setGoalVirtualUsers(goalVirtualUsers);
        setDoNotChangeScriptThinkTime(doNotChangeScriptThinkTime);
        setScenarioSettings(scenarioSettings);
    }

    public static GoalScheduler xmlToObject(String xml) {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("GoalScheduler", GoalScheduler.class);
        xstream.setClassLoader(GoalScheduler.class.getClassLoader());
        xstream.setMode(XStream.NO_REFERENCES);
        return (GoalScheduler) xstream.fromXML(xml);
    }

    @Override
    public String toString() {
        return "GoalScheduler{" + "GoalProfileName = " + GoalProfileName +
                ", GoalType = " + GoalType +
                ", GoalHitsPerSecond = " + GoalHitsPerSecond +
                ", GoalTransactionsPerSecond = " + GoalTransactionsPerSecond +
                ", GoalVirtualUsers = " + GoalVirtualUsers +
                ", DoNotChangeScriptThinkTime = " + DoNotChangeScriptThinkTime +
                ", ScenarioSettings = " + ScenarioSettings +
                "}";
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        //xstream.useAttributeFor(Content.class, "xmlns");
        xstream.alias("GoalScheduler", GoalScheduler.class);
        xstream.aliasField("GoalProfileName", GoalScheduler.class, "GoalProfileName");
        xstream.aliasField("GoalType", GoalScheduler.class, "GoalType");
        xstream.aliasField("GoalHitsPerSecond", GoalScheduler.class, "GoalHitsPerSecond");
        xstream.aliasField("GoalTransactionsPerSecond", GoalScheduler.class, "GoalTransactionsPerSecond");
        xstream.aliasField("GoalVirtualUsers", GoalScheduler.class, "GoalVirtualUsers");
        xstream.aliasField("DoNotChangeScriptThinkTime", GoalScheduler.class, "DoNotChangeScriptThinkTime");
        xstream.aliasField("ScenarioSettings", GoalScheduler.class, "ScenarioSettings");
        xstream.aliasField("GoalScheduler", GoalScheduler.class, "GoalScheduler");
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream.toXML(this);
    }

    public String getGoalProfileName() {
        return GoalProfileName;
    }

    public void setGoalProfileName(String goalProfileName) {
        GoalProfileName = goalProfileName;
    }

    public String getGoalType() {
        return GoalType;
    }

    public void setGoalType(String goalType) {
        this.GoalType = goalType;
    }

    public void setGoalType(GoalTypeValues goalType) {
        this.GoalType = goalType.value();
    }

    public GoalHitsPerSecond getGoalHitsPerSecond() {
        return GoalHitsPerSecond;
    }

    public void setGoalHitsPerSecond(GoalHitsPerSecond goalHitsPerSecond) {
        GoalHitsPerSecond = goalHitsPerSecond;
    }

    public GoalTransactionsPerSecond getGoalTransactionsPerSecond() {
        return GoalTransactionsPerSecond;
    }

    public void setGoalTransactionsPerSecond(GoalTransactionsPerSecond goalTransactionsPerSecond) {
        GoalTransactionsPerSecond = goalTransactionsPerSecond;
    }

    public GoalVirtualUsers getGoalVirtualUsers() {
        return GoalVirtualUsers;
    }

    public void setGoalVirtualUsers(GoalVirtualUsers goalVirtualUsers) {
        GoalVirtualUsers = goalVirtualUsers;
    }

    public boolean isDoNotChangeScriptThinkTime() {
        return DoNotChangeScriptThinkTime;
    }

    public void setDoNotChangeScriptThinkTime(boolean doNotChangeScriptThinkTime) {
        DoNotChangeScriptThinkTime = doNotChangeScriptThinkTime;
    }

    public ScenarioSettings getScenarioSettings() {
        return ScenarioSettings;
    }

    public void setScenarioSettings(ScenarioSettings scenarioSettings) {
        ScenarioSettings = scenarioSettings;
    }
}
