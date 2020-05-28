package io.github.delirius325.jmeter.config.livechanges.helpers;

import org.apache.jmeter.threads.JMeterContextService;

public class GenericHelper {
    public static long getTestTimeElapsedSec() {
        return (System.currentTimeMillis() - JMeterContextService.getTestStartTime()) / 1000;
    }

    public static long getTestTimeElapsedMs() {
        return System.currentTimeMillis() - JMeterContextService.getTestStartTime();
    }
}
