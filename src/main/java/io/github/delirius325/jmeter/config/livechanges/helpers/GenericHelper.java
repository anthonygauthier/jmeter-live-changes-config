package io.github.delirius325.jmeter.config.livechanges.helpers;

import org.apache.jmeter.threads.JMeterContextService;

/**
 * Class containing generic helper functions
 */
public class GenericHelper {
    /**
     * Get the Test's time elapsed in seconds
     * @return time in seconds
     */
    public static long getTestTimeElapsedSec() {
        return (System.currentTimeMillis() - JMeterContextService.getTestStartTime()) / 1000;
    }

    /**
     * Get the Test's time elapsed in milliseconds
     * @return time in milliseconds
     */
    public static long getTestTimeElapsedMs() {
        return System.currentTimeMillis() - JMeterContextService.getTestStartTime();
    }
}
