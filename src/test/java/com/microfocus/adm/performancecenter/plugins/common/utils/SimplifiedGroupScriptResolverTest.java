package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcScript;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcScripts;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.SimplifiedGroup;
import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SimplifiedGroupScriptResolverTest {

    @Test
    public void resolve_withScriptPath_populatesScriptMetadataAndDefaultName() throws Exception {
        PcRestProxy pcRestProxy = mock(PcRestProxy.class);

        PcScript listedScript = new PcScript();
        listedScript.setID(101);
        listedScript.setName("MyScript.usr");
        listedScript.setTestFolderPath("Subject\\FolderA");
        listedScript.setProtocol("WEB");

        PcScripts pcScripts = new PcScripts();
        ArrayList<PcScript> list = new ArrayList<PcScript>();
        list.add(listedScript);
        pcScripts.setPcScriptList(list);
        when(pcRestProxy.getScripts()).thenReturn(pcScripts);

        SimplifiedGroup group = new SimplifiedGroup();
        group.setScript_id(0);
        group.setScript_path("FolderA/MyScript.usr");

        SimplifiedContent content = new SimplifiedContent();
        content.setGroup(Collections.singletonList(group));

        SimplifiedContent resolved = new SimplifiedGroupScriptResolver(pcRestProxy).resolve(content);

        SimplifiedGroup resolvedGroup = resolved.getGroup().get(0);
        Assert.assertEquals(101, resolvedGroup.getScript_id());
        Assert.assertEquals("WEB", resolvedGroup.getProtocol());
        Assert.assertEquals("MyScript.usr_1", resolvedGroup.getGroup_name());
    }

    @Test
    public void resolve_withScriptId_populatesProtocolAndPath() throws Exception {
        PcRestProxy pcRestProxy = mock(PcRestProxy.class);

        PcScript scriptById = new PcScript();
        scriptById.setID(202);
        scriptById.setName("ApiScript");
        scriptById.setTestFolderPath("Subject\\Api");
        scriptById.setProtocol("TRUAPI");
        when(pcRestProxy.getScript(202)).thenReturn(scriptById);

        SimplifiedGroup group = new SimplifiedGroup();
        group.setScript_id(202);
        group.setScript_path(null);

        SimplifiedContent content = new SimplifiedContent();
        content.setGroup(Collections.singletonList(group));

        SimplifiedContent resolved = new SimplifiedGroupScriptResolver(pcRestProxy).resolve(content);

        SimplifiedGroup resolvedGroup = resolved.getGroup().get(0);
        Assert.assertEquals("TRUAPI", resolvedGroup.getProtocol());
        Assert.assertEquals("Subject\\Api", resolvedGroup.getScript_path());
        Assert.assertEquals("ApiScript_1", resolvedGroup.getGroup_name());
    }
}

