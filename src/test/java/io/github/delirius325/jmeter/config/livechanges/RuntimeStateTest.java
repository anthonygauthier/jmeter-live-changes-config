package io.github.delirius325.jmeter.config.livechanges;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RuntimeStateTest {
    @Test
    public void defaultsToSingleNodeWhenReset() {
        RuntimeState runtimeState = new RuntimeState();

        runtimeState.resetForTestStart();

        assertEquals(ExecutionMode.SINGLE_NODE, runtimeState.getExecutionMode());
        assertTrue(runtimeState.getRemoteHosts().isEmpty());
        assertFalse(runtimeState.isApiStarted());
        assertFalse(runtimeState.hasStartupFailure());
    }

    @Test
    public void switchesToDistributedControllerWhenRemoteHostRegisters() {
        RuntimeState runtimeState = new RuntimeState();

        runtimeState.registerRemoteHost("worker-a");

        assertEquals(ExecutionMode.DISTRIBUTED_CONTROLLER, runtimeState.getExecutionMode());
        assertTrue(runtimeState.getRemoteHosts().contains("worker-a"));
    }

    @Test
    public void clearsFailureWhenApiStarts() {
        RuntimeState runtimeState = new RuntimeState();

        runtimeState.markStartupFailure("boom");
        runtimeState.markApiStarted();

        assertTrue(runtimeState.isApiStarted());
        assertFalse(runtimeState.hasStartupFailure());
    }
}
