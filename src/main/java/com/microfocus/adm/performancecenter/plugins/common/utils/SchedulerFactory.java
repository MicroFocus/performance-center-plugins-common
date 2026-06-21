package com.microfocus.adm.performancecenter.plugins.common.utils;

import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.Scheduler;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.Action;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.common.Ramp;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.common.TimeInterval;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.duration.Duration;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.initialize.Initialize;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.startvusers.StartVusers;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.content.scheduler.actions.stopvusers.StopVusers;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.DurationTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.pcsubentities.test.enums.StartStopVusersTypeValues;
import com.microfocus.adm.performancecenter.plugins.common.pcentities.simplifiedentities.simplifiedtest.content.SimplifiedContent;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

final class SchedulerFactory {
    private static final int MIN_INTERVAL_TIME = 15;

    Scheduler build(SimplifiedContent simplifiedContent) {
        ArrayList<Action> actions = new ArrayList<Action>();
        Initialize initialize = new Initialize();
        StartVusers startVusers = getStartVusersSchedulerByTest(simplifiedContent);
        Duration duration = new Duration();

        if (simplifiedContent.getScheduler().getDuration() > 0) {
            TimeInterval timeInterval = getTimeInterval(simplifiedContent.getScheduler().getDuration());
            duration = new Duration(DurationTypeValues.RUN_FOR, timeInterval);
        }

        actions.add(new Action(initialize));
        actions.add(new Action(startVusers));
        actions.add(new Action(duration));
        actions.add(new Action(stopVusers()));
        return new Scheduler(actions);
    }

    private StopVusers stopVusers() {
        return new StopVusers();
    }

    // using 15 seconds interval
    private StartVusers getStartVusersSchedulerByTest(SimplifiedContent simplifiedContent) {
        StartVusers startVusers;
        if (simplifiedContent.getScheduler().getRampup() > 30) {
            int vusersSum = simplifiedContent.getGroup().stream().filter(o -> o.getVusers() > 0).mapToInt(o -> o.getVusers()).sum();

            double exactTimeIntervalInSecondsPerUser = ((double) simplifiedContent.getScheduler().getRampup()) / ((double) vusersSum);
            int vusers = 1;
            int timeIntervalInSeconds = (int) exactTimeIntervalInSecondsPerUser;
            if (exactTimeIntervalInSecondsPerUser < MIN_INTERVAL_TIME && exactTimeIntervalInSecondsPerUser > 0) {
                vusers = (int) (((double) MIN_INTERVAL_TIME) / exactTimeIntervalInSecondsPerUser)
                        + ((((double) MIN_INTERVAL_TIME) % exactTimeIntervalInSecondsPerUser) == 0 ? 0 : 1);
                timeIntervalInSeconds = MIN_INTERVAL_TIME;
            }
            TimeInterval timeInterval = getTimeInterval(timeIntervalInSeconds);
            Ramp ramp = new Ramp(vusers, timeInterval);
            startVusers = new StartVusers(StartStopVusersTypeValues.GRADUALLY, ramp);
        } else if (simplifiedContent.getScheduler().getRampup() > 1) {
            int vusersSum = simplifiedContent.getGroup().stream().filter(o -> o.getVusers() > 0).mapToInt(o -> o.getVusers()).sum();
            int timeIntervalInSeconds = simplifiedContent.getScheduler().getRampup() / 2;
            int vusers = (vusersSum / 2) + (((vusersSum % 2) == 0) ? 0 : 1);
            TimeInterval timeInterval = getTimeInterval(timeIntervalInSeconds);
            Ramp ramp = new Ramp(vusers, timeInterval);
            startVusers = new StartVusers(StartStopVusersTypeValues.GRADUALLY, ramp);
        } else {
            startVusers = new StartVusers();
        }

        return startVusers;
    }

    private TimeInterval getTimeInterval(int timeIntervalInSeconds) {
        int day = (int) TimeUnit.SECONDS.toDays(timeIntervalInSeconds);
        long hours = TimeUnit.SECONDS.toHours(timeIntervalInSeconds) - (day * 24);
        long minute = TimeUnit.SECONDS.toMinutes(timeIntervalInSeconds) - (TimeUnit.SECONDS.toHours(timeIntervalInSeconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(timeIntervalInSeconds) - (TimeUnit.SECONDS.toMinutes(timeIntervalInSeconds) * 60);
        return new TimeInterval(day, Math.toIntExact(hours), Math.toIntExact(minute), Math.toIntExact(second));
    }
}

