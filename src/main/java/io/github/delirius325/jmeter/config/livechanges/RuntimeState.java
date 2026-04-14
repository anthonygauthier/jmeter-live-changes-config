package io.github.delirius325.jmeter.config.livechanges;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds runtime state that the API can expose while JMeter is running.
 */
public class RuntimeState {
    private ExecutionMode executionMode;
    private final Set<String> remoteHosts;
    private boolean apiStarted;
    private String startupFailureMessage;

    public RuntimeState() {
        this.remoteHosts = ConcurrentHashMap.newKeySet();
        this.resetForTestStart();
    }

    public synchronized void resetForTestStart() {
        this.executionMode = ExecutionMode.SINGLE_NODE;
        this.remoteHosts.clear();
        this.apiStarted = false;
        this.startupFailureMessage = null;
    }

    public synchronized void registerRemoteHost(String host) {
        if (host != null && !host.trim().isEmpty()) {
            this.remoteHosts.add(host.trim());
            this.executionMode = ExecutionMode.DISTRIBUTED_CONTROLLER;
        }
    }

    public synchronized void markApiStarted() {
        this.apiStarted = true;
        this.startupFailureMessage = null;
    }

    public synchronized void markStartupFailure(String message) {
        this.apiStarted = false;
        this.startupFailureMessage = message;
    }

    public synchronized ExecutionMode getExecutionMode() {
        return this.executionMode;
    }

    public synchronized boolean isApiStarted() {
        return this.apiStarted;
    }

    public synchronized boolean hasStartupFailure() {
        return this.startupFailureMessage != null && !this.startupFailureMessage.isEmpty();
    }

    public synchronized String getStartupFailureMessage() {
        return this.startupFailureMessage;
    }

    public Set<String> getRemoteHosts() {
        return Collections.unmodifiableSet(this.remoteHosts);
    }
}
