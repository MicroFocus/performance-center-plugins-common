package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.PcScript;
import com.microfocus.adm.performancecenter.plugins.common.rest.PcRestProxy;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConvertContentStringToTestTest {

    private static final String SIMPLIFIED_TEST_YAML =
            "test_name: myFromYaml\n" +
            "test_folder_path: folder/sub\n" +
            "test_content:\n" +
            "  group:\n" +
            "    - script_id: 101\n" +
            "      vusers: 2\n" +
            "  scheduler:\n" +
            "    rampup: 1\n" +
            "    duration: 60\n";

    @Test
    public void convert_returnsExpectedResultFields() throws Exception {
        PcRestProxy pcRestProxy = mockPcRestProxyWithScript();

        ConvertContentStringToTest.ConversionResult result =
                new ConvertContentStringToTest(pcRestProxy).convert("", "", SIMPLIFIED_TEST_YAML);

        Assert.assertEquals("myFromYaml", result.getTestName());
        Assert.assertEquals("folder/sub", result.getTestFolderPath());
        Assert.assertEquals("Subject\\folder\\sub", result.getTestFolderPathWithSubject());
        Assert.assertNotNull(result.getContent());
        Assert.assertNotNull(result.getContent().getScheduler());
        Assert.assertNotNull(result.getContent().getGroups());
        Assert.assertEquals(1, result.getContent().getGroups().size());
        Assert.assertEquals("ApiScript_1", result.getContent().getGroups().get(0).getName());
        Assert.assertEquals("101", result.getContent().getGroups().get(0).getScript().getID());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void invoke_remainsBackwardCompatibleWithConvert() throws Exception {
        PcRestProxy pcRestProxy = mockPcRestProxyWithScript();

        ConvertContentStringToTest.ConversionResult result =
                new ConvertContentStringToTest(pcRestProxy).convert("", "", SIMPLIFIED_TEST_YAML);

        ConvertContentStringToTest legacy = new ConvertContentStringToTest(pcRestProxy, "", "", SIMPLIFIED_TEST_YAML).invoke();

        Assert.assertEquals(result.getTestName(), legacy.getTestName());
        Assert.assertEquals(result.getTestFolderPath(), legacy.getTestFolderPath());
        Assert.assertEquals(result.getTestFolderPathWithSubject(), legacy.getTestFolderPathWithSubject());
        Assert.assertNotNull(legacy.getContent());
        Assert.assertEquals(result.getContent().getGroups().get(0).getName(), legacy.getContent().getGroups().get(0).getName());
        Assert.assertEquals(result.getContent().getGroups().get(0).getScript().getID(), legacy.getContent().getGroups().get(0).getScript().getID());
    }

    private PcRestProxy mockPcRestProxyWithScript() throws Exception {
        PcRestProxy pcRestProxy = mock(PcRestProxy.class);
        PcScript script = new PcScript();
        script.setID(101);
        script.setName("ApiScript");
        script.setTestFolderPath("Subject\\Api");
        script.setProtocol("TRUAPI");
        when(pcRestProxy.getScript(101)).thenReturn(script);
        return pcRestProxy;
    }
}
