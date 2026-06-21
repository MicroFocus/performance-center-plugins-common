package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.RTS;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.javavm.JavaVM;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.javavm.javaenvclasspaths.JavaEnvClassPaths;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.jmeter.JMeter;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.log.Log;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.pacing.Pacing;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.pacing.startnewiteration.StartNewIteration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.selenium.Selenium;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.groups.rts.thinktime.ThinkTime;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.StartNewIterationTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.SimplifiedGroup;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.rts.javavm.SimplifiedJavaVM;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.rts.jmeter.SimplifiedJMeter;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.rts.pacing.SimplifiedPacing;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.rts.selenium.SimplifiedSelenium;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.rts.thinktime.SimplifiedThinkTime;

import java.util.ArrayList;

final class GroupRtsFactory {

    RTS create(SimplifiedGroup simplifiedGroup) {
        Pacing pacing = null;
        JavaVM javaVM = null;
        Log log = null;
        JMeter jMeter = null;
        ThinkTime thinkTime = null;
        Selenium selenium = null;
        if (simplifiedGroup.getRts() != null) {
            pacing = definePacing(simplifiedGroup);
            javaVM = defineJavaVM(simplifiedGroup);
            thinkTime = defineThinkTime(simplifiedGroup);
            jMeter = defineJMeter(simplifiedGroup);
            selenium = defineSelenium(simplifiedGroup);
        }
        return new RTS(pacing, thinkTime, log, jMeter, javaVM, selenium);
    }

    private Pacing definePacing(SimplifiedGroup simplifiedGroup) {
        SimplifiedPacing simplifiedPacing = simplifiedGroup.getRts().getPacing();
        if (simplifiedPacing != null && simplifiedPacing.getNumber_of_iterations() > 0) {
            StartNewIteration startNewIteration = new StartNewIteration();
            if (simplifiedPacing.getType() != null && !simplifiedPacing.getType().isEmpty() && simplifiedPacing.getDelay() > 0) {
                if (simplifiedPacing.getType().equalsIgnoreCase(StartNewIterationTypeValues.FIXED_DELAY.value())
                        || simplifiedPacing.getType().equalsIgnoreCase(StartNewIterationTypeValues.FIXED_INTERVAL.value())) {
                    startNewIteration = new StartNewIteration(
                            simplifiedPacing.getType().equalsIgnoreCase(StartNewIterationTypeValues.FIXED_DELAY.value())
                                    ? StartNewIterationTypeValues.FIXED_DELAY.value()
                                    : StartNewIterationTypeValues.FIXED_INTERVAL.value(),
                            simplifiedPacing.getDelay(),
                            -1,
                            -1);
                }
                if ((simplifiedPacing.getType().equalsIgnoreCase(StartNewIterationTypeValues.RANDOM_DELAY.value())
                        || simplifiedPacing.getType().equalsIgnoreCase(StartNewIterationTypeValues.RANDOM_INTERVAL.value()))
                        && simplifiedPacing.getDelay_random_range() > 0) {
                    startNewIteration = new StartNewIteration(
                            simplifiedPacing.getType().equalsIgnoreCase(StartNewIterationTypeValues.RANDOM_DELAY.value())
                                    ? StartNewIterationTypeValues.RANDOM_DELAY.value()
                                    : StartNewIterationTypeValues.RANDOM_INTERVAL.value(),
                            -1,
                            simplifiedPacing.getDelay(),
                            simplifiedPacing.getDelay() + simplifiedPacing.getDelay_random_range());
                }
            }
            return new Pacing(simplifiedPacing.getNumber_of_iterations(), startNewIteration);
        }
        return null;
    }

    private JMeter defineJMeter(SimplifiedGroup simplifiedGroup) {
        SimplifiedJMeter simplifiedJMeter = simplifiedGroup.getRts().getJmeter();
        if (simplifiedJMeter != null) {
            boolean useJMeterAdditionalProperties =
                    simplifiedJMeter.getJmeter_additional_properties() != null
                            && !simplifiedJMeter.getJmeter_additional_properties().isEmpty();
            return new JMeter(simplifiedJMeter.isStart_measurements(),
                    simplifiedJMeter.getJmeter_home_path(),
                    !(simplifiedJMeter.getJmeter_min_port() > 0
                            && simplifiedJMeter.getJmeter_max_port() > simplifiedJMeter.getJmeter_min_port()),
                    simplifiedJMeter.getJmeter_min_port(),
                    simplifiedJMeter.getJmeter_max_port(),
                    useJMeterAdditionalProperties,
                    useJMeterAdditionalProperties ? simplifiedJMeter.getJmeter_additional_properties() : null);
        }
        return null;
    }

    private Selenium defineSelenium(SimplifiedGroup simplifiedGroup) {
        SimplifiedSelenium simplifiedSelenium = simplifiedGroup.getRts().getSelenium();
        if (simplifiedSelenium != null) {
            return new Selenium(simplifiedSelenium.getJre_path(),
                    simplifiedSelenium.getClass_path(),
                    simplifiedSelenium.getTest_ng_files());
        }
        return null;
    }

    private ThinkTime defineThinkTime(SimplifiedGroup simplifiedGroup) {
        SimplifiedThinkTime simplifiedThinkTime = simplifiedGroup.getRts().getThinktime();
        if (simplifiedThinkTime != null && simplifiedThinkTime.getType() != null && !simplifiedThinkTime.getType().isEmpty()) {
            return new ThinkTime(simplifiedThinkTime.getType(),
                    simplifiedThinkTime.getLimit_seconds(),
                    simplifiedThinkTime.getMin_percentage(),
                    simplifiedThinkTime.getMax_percentage(),
                    simplifiedThinkTime.getMultiply_factor());
        }
        return null;
    }

    private JavaVM defineJavaVM(SimplifiedGroup simplifiedGroup) {
        SimplifiedJavaVM simplifiedJavaVM = simplifiedGroup.getRts().getJava_vm();
        if (simplifiedJavaVM != null) {
            boolean userSpecifiedJdk = (simplifiedJavaVM.getJdk_home() != null && !simplifiedJavaVM.getJdk_home().isEmpty());
            JavaEnvClassPaths javaEnvClassPaths = null;
            if (simplifiedJavaVM.getJava_env_class_paths() != null && simplifiedJavaVM.getJava_env_class_paths().length > 0) {
                javaEnvClassPaths = new JavaEnvClassPaths();
                ArrayList<String> javaEnvClassPath = new ArrayList<String>();
                for (String javaEnvClassPathItem : simplifiedJavaVM.getJava_env_class_paths()) {
                    javaEnvClassPath.add(javaEnvClassPathItem);
                }
                javaEnvClassPaths.setJavaEnvClassPath(javaEnvClassPath);
            }
            return new JavaVM(javaEnvClassPaths,
                    userSpecifiedJdk,
                    userSpecifiedJdk ? simplifiedJavaVM.getJdk_home() : null,
                    simplifiedJavaVM.getJava_vm_parameters(),
                    simplifiedJavaVM.isUse_xboot(),
                    simplifiedJavaVM.isEnable_classloader_per_vuser());
        }
        return null;
    }
}

