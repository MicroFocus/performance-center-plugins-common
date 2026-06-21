package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.Scheduler;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.Action;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.startvusers.StartVusers;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.DurationTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.StartStopVusersTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.group.SimplifiedGroup;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.scheduler.SimplifiedScheduler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class SchedulerFactoryTest {

    @Test
    public void build_usesGradualRampWithMinIntervalWhenRampupIsLarge() {
        SimplifiedContent content = buildContent(2, 40, 120, 600);

        Scheduler scheduler = new SchedulerFactory().build(content);

        Action startAction = scheduler.getActions().get(1);
        StartVusers startVusers = startAction.getStartVusers();
        Assert.assertEquals(StartStopVusersTypeValues.GRADUALLY.value(), startVusers.getType());
        Assert.assertEquals("6", startVusers.getRamp().getVusers());
        Assert.assertEquals("15", startVusers.getRamp().getTimeInterval().getSeconds());

        Action durationAction = scheduler.getActions().get(2);
        Assert.assertEquals(DurationTypeValues.RUN_FOR.value(), durationAction.getDuration().getType());
        Assert.assertEquals("10", durationAction.getDuration().getTimeInterval().getMinutes());
    }

    @Test
    public void build_usesDefaultStartWhenRampupIsOneOrLess() {
        SimplifiedContent content = buildContent(5, 10, 1, 0);

        Scheduler scheduler = new SchedulerFactory().build(content);

        Action startAction = scheduler.getActions().get(1);
        StartVusers startVusers = startAction.getStartVusers();
        Assert.assertEquals(StartStopVusersTypeValues.SIMULTANEOUSLY.value(), startVusers.getType());
        Assert.assertNull(startVusers.getRamp());
    }

    private SimplifiedContent buildContent(int vusersGroupOne, int vusersGroupTwo, int rampup, int duration) {
        SimplifiedGroup groupOne = new SimplifiedGroup();
        groupOne.setVusers(vusersGroupOne);

        SimplifiedGroup groupTwo = new SimplifiedGroup();
        groupTwo.setVusers(vusersGroupTwo);

        SimplifiedContent content = new SimplifiedContent();
        content.setGroup(Arrays.asList(groupOne, groupTwo));
        content.setScheduler(new SimplifiedScheduler(rampup, duration));
        return content;
    }
}

