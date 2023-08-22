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
package com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.common.Common;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.host.Host;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.RTS;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.javavm.javaenvclasspaths.JavaEnvClassPaths;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.log.Log;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.log.logoptions.LogOptions;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.pacing.startnewiteration.StartNewIteration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.thinktime.ThinkTime;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.script.Script;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.Scheduler;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.Action;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.duration.Duration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.initialize.Initialize;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.startgroup.StartGroup;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.startvusers.StartVusers;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.stopvusers.StopVusers;
import com.microfocus.adm.performancecenter.plugins.common.utils.Helper;
import com.thoughtworks.xstream.XStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Group")
public class Group {
    @XmlElement
    private String Name;

    @XmlElement
    private String Vusers;

    @XmlElement
    private Script Script;

    @XmlElement
    private ArrayList<Host> Hosts;

    @XmlElement
    private RTS RTS;

    @XmlElement
    private String GlobalCommandLine;

    @XmlElement
    private String CommandLine;

    @XmlElement
    private String GlobalRTS;

    @XmlElement
    private Scheduler Scheduler;

    public Group() {
    }

    public Group(String name, int vusers, Script script, ArrayList<Host> hosts, RTS RTS, String globalCommandLine, String commandLine, String globalRTS) {
        setName(name);
        setVusers(vusers);
        setScript(script);
        setHosts(hosts);
        setRTS(RTS);
        setGlobalCommandLine(globalCommandLine);
        setCommandLine(commandLine);
        setGlobalRTS(globalRTS);
    }

    public Group(String name, String vusers, Script script, ArrayList<Host> hosts, RTS RTS, String globalCommandLine, String commandLine, String globalRTS) {
        setName(name);
        setVusers(vusers);
        setScript(script);
        setHosts(hosts);
        setRTS(RTS);
        setGlobalCommandLine(globalCommandLine);
        setCommandLine(commandLine);
        setGlobalRTS(globalRTS);
    }

    public Group(String name, int vusers, Script script, ArrayList<Host> hosts, RTS RTS, String globalCommandLine, String commandLine, String globalRTS, Scheduler scheduler) {
        setName(name);
        setVusers(vusers);
        setScript(script);
        setHosts(hosts);
        setRTS(RTS);
        setGlobalCommandLine(globalCommandLine);
        setCommandLine(commandLine);
        setGlobalRTS(globalRTS);
        setScheduler(scheduler);
    }

    public Group(String name, String vusers, Script script, ArrayList<Host> hosts, RTS RTS, String globalCommandLine, String commandLine, String globalRTS, Scheduler scheduler) {
        setName(name);
        setVusers(vusers);
        setScript(script);
        setHosts(hosts);
        setRTS(RTS);
        setGlobalCommandLine(globalCommandLine);
        setCommandLine(commandLine);
        setGlobalRTS(globalRTS);
        setScheduler(scheduler);
    }

    public static Group xmlToObject(String xml) {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("Group", Group.class);

        xstream.aliasField("Pacing", RTS.class, "Pacing");
        xstream.useAttributeFor(StartNewIteration.class, "Type");
        xstream.aliasField("Type", StartNewIteration.class, "Type");
        xstream.aliasField("ThinkTime", RTS.class, "ThinkTime");
        xstream.useAttributeFor(ThinkTime.class, "Type");
        xstream.aliasField("Type", ThinkTime.class, "Type");
        xstream.aliasField("Log", RTS.class, "Log");
        xstream.useAttributeFor(Log.class, "Type");
        xstream.aliasField("LogOptions", Log.class, "LogOptions");
        xstream.useAttributeFor(LogOptions.class, "Type");
        xstream.aliasField("JMeter", RTS.class, "JMeter");
        xstream.alias("Host", Host.class, Host.class);
        xstream.useAttributeFor(StopVusers.class, "Type");
        xstream.aliasField("Type", StopVusers.class, "Type");
        xstream.useAttributeFor(StartVusers.class, "Type");
        xstream.aliasField("Type", StartVusers.class, "Type");
        xstream.useAttributeFor(StartGroup.class, "Type");
        xstream.aliasField("Type", StartGroup.class, "Type");
        xstream.useAttributeFor(Initialize.class, "Type");
        xstream.aliasField("Type", Initialize.class, "Type");
        xstream.useAttributeFor(Duration.class, "Type");
        xstream.aliasField("Type", Duration.class, "Type");
        xstream.alias("Action", Action.class, Action.class);
        xstream.omitField(Script.class, "ProtocolType");

        //JavaEnvClassPaths
        xstream.alias("JavaEnvClassPath", String.class);
        xstream.addImplicitCollection(JavaEnvClassPaths.class, "JavaEnvClassPath", "JavaEnvClassPath", String.class);

        xstream.setClassLoader(Group.class.getClassLoader());
        xstream.setMode(XStream.NO_REFERENCES);
        return (Group) xstream.fromXML(xml);
    }

    @Override
    public String toString() {
        return "Group{" + "Name = " + Name +
                ", Vusers = " + Vusers +
                ", Script = " + Script +
                ", Hosts = " + Hosts +
                ", GlobalCommandLine = " + GlobalCommandLine +
                ", CommandLine = " + CommandLine +
                ", GlobalRTS = " + GlobalRTS +
                ", RTS = " + RTS +
                ", SimplifiedScheduler = " + Scheduler + "}";
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream = Helper.xstreamPermissions(xstream);
        xstream.alias("Group", Group.class);
        xstream.aliasField("Name", Group.class, "Name");

        xstream.aliasField("Pacing", RTS.class, "Pacing");
        xstream.useAttributeFor(StartNewIteration.class, "Type");
        xstream.aliasField("Type", StartNewIteration.class, "Type");
        xstream.aliasField("ThinkTime", RTS.class, "ThinkTime");
        xstream.useAttributeFor(ThinkTime.class, "Type");
        xstream.aliasField("Type", ThinkTime.class, "Type");
        xstream.aliasField("Log", RTS.class, "Log");
        xstream.useAttributeFor(Log.class, "Type");
        xstream.aliasField("LogOptions", Log.class, "LogOptions");
        xstream.useAttributeFor(LogOptions.class, "Type");
        xstream.aliasField("JMeter", RTS.class, "JMeter");
        xstream.alias("Host", Host.class, Host.class);
        xstream.useAttributeFor(StopVusers.class, "Type");
        xstream.aliasField("Type", StopVusers.class, "Type");
        xstream.useAttributeFor(StartVusers.class, "Type");
        xstream.aliasField("Type", StartVusers.class, "Type");
        xstream.useAttributeFor(StartGroup.class, "Type");
        xstream.aliasField("Type", StartGroup.class, "Type");
        xstream.useAttributeFor(Initialize.class, "Type");
        xstream.aliasField("Type", Initialize.class, "Type");
        xstream.useAttributeFor(Duration.class, "Type");
        xstream.aliasField("Type", Duration.class, "Type");
        xstream.alias("Action", Action.class, Action.class);

        xstream.aliasField("Vusers", Group.class, "Vusers");
        xstream.aliasField("Script", Group.class, "Script");
        xstream.aliasField("Hosts", Group.class, "Hosts");
        xstream.aliasField("RTS", Group.class, "RTS");

        //JavaEnvClassPaths
        xstream.alias("JavaEnvClassPath", String.class);
        xstream.addImplicitCollection(JavaEnvClassPaths.class, "JavaEnvClassPath", "JavaEnvClassPath", String.class);

        xstream.aliasField("GlobalCommandLine", Group.class, "GlobalCommandLine");
        xstream.aliasField("CommandLine", Group.class, "CommandLine");
        xstream.aliasField("GlobalRTS", Group.class, "GlobalRTS");
        xstream.aliasField("SimplifiedScheduler", Group.class, "SimplifiedScheduler");
        xstream.aliasField("Group", Group.class, "Group");
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream.toXML(this);
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getVusers() {
        return Vusers;
    }

    public void setVusers(int value) {
        this.Vusers = Common.integerToString(value);
    }

    public void setVusers(String value) {
        this.Vusers = value;
    }

    public Script getScript() {
        return Script;
    }

    public void setScript(Script script) {
        Script = script;
    }

    public ArrayList<Host> getHosts() {
        return Hosts;
    }

    public void setHosts(ArrayList<Host> hosts) {
        Hosts = hosts;
    }

    public RTS getRTS() {
        return RTS;
    }

    public void setRTS(RTS RTS) {
        this.RTS = RTS;
    }

    public String getGlobalCommandLine() {
        return GlobalCommandLine;
    }

    public void setGlobalCommandLine(String globalCommandLine) {
        GlobalCommandLine = globalCommandLine;
    }

    public String getCommandLine() {
        return CommandLine;
    }

    public void setCommandLine(String commandLine) {
        CommandLine = commandLine;
    }

    public String getGlobalRTS() {
        return GlobalRTS;
    }

    public void setGlobalRTS(String globalRTS) {
        GlobalRTS = globalRTS;
    }

    public Scheduler getScheduler() {
        return Scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        Scheduler = scheduler;
    }
}