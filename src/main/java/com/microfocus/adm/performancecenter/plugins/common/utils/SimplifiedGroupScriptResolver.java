package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcException;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcScript;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.SimplifiedGroup;
import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;

import java.io.File;
import java.io.IOException;
import java.util.List;

final class SimplifiedGroupScriptResolver {

    private final PcRestProxy pcRestProxy;

    SimplifiedGroupScriptResolver(PcRestProxy pcRestProxy) {
        this.pcRestProxy = pcRestProxy;
    }

    SimplifiedContent resolve(SimplifiedContent simplifiedContent) throws IOException, PcException {
        List<SimplifiedGroup> simplifiedGroups = simplifiedContent.getGroup();
        int index = 0;
        ScriptCache scriptCache = null;

        for (SimplifiedGroup simplifiedGroup : simplifiedGroups) {
            index++;
            PcScript pcScript = null;

            if (simplifiedGroup.getScript_id() == 0
                    && simplifiedGroup.getScript_path() != null
                    && !simplifiedGroup.getScript_path().isEmpty()) {
                if (scriptCache == null) {
                    scriptCache = new ScriptCache(pcRestProxy.getScripts().getPcScriptList());
                }

                File file = new File("Subject\\".concat(simplifiedGroup.getScript_path()));
                String scriptFolderPath = Helper.getParent(file.toPath()).toString();
                String scriptName = Helper.getName(file.getName());

                pcScript = scriptCache.getScript(scriptFolderPath, scriptName);
                simplifiedGroup.setScript_id(pcScript.getID());
                simplifiedGroup.setProtocol(pcScript.getProtocol());
            } else if (simplifiedGroup.getScript_id() > 0) {
                pcScript = pcRestProxy.getScript(simplifiedGroup.getScript_id());
                simplifiedGroup.setProtocol(pcScript.getProtocol());
                simplifiedGroup.setScript_path(pcScript.getTestFolderPath());
            }

            if ((simplifiedGroup.getGroup_name() == null || simplifiedGroup.getGroup_name().isEmpty()) && pcScript != null) {
                simplifiedGroup.setGroup_name(pcScript.getName().concat("_").concat(Integer.toString(index)));
            }
        }

        simplifiedContent.setGroup(simplifiedGroups);
        return simplifiedContent;
    }
}

