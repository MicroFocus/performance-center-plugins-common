package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.globalcommandline.GlobalCommandLine;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.globalcommandline.commandline.CommandLine;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.Group;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.host.Host;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.RTS;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.script.Script;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.lgdistribution.LGDistribution;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.HostTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.LGDistributionTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.SimplifiedGroup;

import java.util.ArrayList;
import java.util.List;

final class GroupFactory {

    private final GroupRtsFactory groupRtsFactory;

    GroupFactory() {
        this(new GroupRtsFactory());
    }

    GroupFactory(GroupRtsFactory groupRtsFactory) {
        this.groupRtsFactory = groupRtsFactory;
    }

    ArrayList<Group> createGroups(List<SimplifiedGroup> simplifiedGroups, LGDistribution lgDistribution) {
        ArrayList<Group> receivedGroups = new ArrayList<Group>();
        for (SimplifiedGroup simplifiedGroup : simplifiedGroups) {
            String groupName = simplifiedGroup.getGroup_name();
            int groupVusers = simplifiedGroup.getVusers() > 0 ? simplifiedGroup.getVusers() : 1;
            Script groupScript = new Script(simplifiedGroup.getScript_id());
            RTS groupRTS = groupRtsFactory.create(simplifiedGroup);
            String globalCommandLine = defineGlobalCommandLine(simplifiedGroup);
            ArrayList<Host> groupHosts = defineGroupHosts(lgDistribution, simplifiedGroup);
            Group group = new Group(groupName, groupVusers, groupScript, groupHosts, groupRTS, globalCommandLine, null, null);
            receivedGroups.add(group);
        }
        return receivedGroups;
    }

    GlobalCommandLine buildGlobalCommandLine(List<SimplifiedGroup> simplifiedGroups) {
        ArrayList<CommandLine> commandLines = new ArrayList<CommandLine>();
        for (SimplifiedGroup simplifiedGroup : simplifiedGroups) {
            if (simplifiedGroup.getCommand_line() != null && !simplifiedGroup.getCommand_line().isEmpty()) {
                commandLines.add(new CommandLine(simplifiedGroup.getGroup_name(), simplifiedGroup.getCommand_line()));
            }
        }
        return commandLines.isEmpty() ? null : new GlobalCommandLine(commandLines);
    }

    private String defineGlobalCommandLine(SimplifiedGroup simplifiedGroup) {
        if (simplifiedGroup.getCommand_line() != null && !simplifiedGroup.getCommand_line().isEmpty()) {
            return simplifiedGroup.getGroup_name();
        }
        return null;
    }

    private ArrayList<Host> defineGroupHosts(LGDistribution lgDistribution, SimplifiedGroup simplifiedGroup) {
        ArrayList<Host> groupHosts = new ArrayList<Host>();
        if (LGDistributionTypeValues.MANUAL.value().equals(lgDistribution.getType())) {
            for (String lgHost : simplifiedGroup.getLg_name()) {
                if (lgHost.startsWith("LG") && Character.isDigit(lgHost.charAt(lgHost.length() - 1))) {
                    groupHosts.add(new Host(lgHost, HostTypeValues.AUTOMATCH));
                } else if (lgHost.startsWith("DOCKER") && Character.isDigit(lgHost.charAt(lgHost.length() - 1))) {
                    groupHosts.add(new Host(lgHost, HostTypeValues.DYNAMIC));
                } else {
                    groupHosts.add(new Host(lgHost, HostTypeValues.SPECIFIC));
                }
            }
            return groupHosts;
        }
        return null;
    }
}
